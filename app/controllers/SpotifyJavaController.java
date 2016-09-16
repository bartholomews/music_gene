package controllers;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.BadRequestException;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.CurrentUserRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.*;
import scala.Option;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller to interface the Java Spotify API wrapper
 * This code is adapted from the wrapper's README on Github:
 * {@see https://github.com/thelinmichael/spotify-web-api-java}
 */
public class SpotifyJavaController {
    private static volatile SpotifyJavaController instance;
    private static final Object lock = new Object();
    private static final String CLIENT_ID = "24c87b0353a141768e9b842eb7bd0f67";
    private static final String CLIENT_SECRET = "cc5d6ebca4b445c782b6aced791710ab";
    private static final String REDIRECT_URI = "http://localhost:9000/callback";
//    private static final String REDIRECT_URI = "https://musicgene.herokuapp.com/callback";

    private SpotifyJavaController() {}

    /**
     * {@see} http://stackoverflow.com/a/11165926
     *
     * @return an instance of SpotifyController
     */
    public static SpotifyJavaController getInstance() {
        SpotifyJavaController i;
        if (instance == null) {
            synchronized (lock) {   // While waiting for the lock, another
                i = instance;       // thread may have instantiated the object.
                if (i == null) {
                    i = new SpotifyJavaController();
                    instance = i;
                }
            }
        }
        return instance;
    }

    /**
     * API endpoint instance of the Spotify wrapper
     */
    private static final Api api = Api.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .redirectURI(REDIRECT_URI)
            .build();

    /**
     * Set the necessary scopes that the application will need from the user
     */
    private final List<String> scopes = Arrays.asList("user-read-private", "user-read-email",
            "user-library-read", "user-library-modify", "playlist-modify-public");

    /* Set a state. This is used to prevent cross site request forgeries. */
    private final String state = "musicgene";

    private String authorizeURL = createAuthorizeURL(scopes, state, true);

    /* Create a request object. */
    final ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();

    private String createAuthorizeURL(List<String> scopes, String state, boolean val) {
        return api.createAuthorizeURL(scopes)
                .state(state)
                .showDialog(val)
                .build()
                .toStringWithQueryParameters();
    }

    /**
     * send the user to the authorizeURL;
     * e.g. https://accounts.spotify.com:443/authorize?client_id=5fe01282e44241328a84e7c5cc169165
     * &response_type=code&redirect_uri=https://example.com/callback
     * &scope=user-read-private%20user-read-email&state=some-state-of-my-choice
     *
     * @return the Spotify url to prompt the user for authorization
     */
    public String getAuthorizeURL() {
        return authorizeURL;
    }

    /**
     * Use the request object to make the request
     */
    public void getAccessToken(String code) {
        String token = null;
        // Make a token request. Asynchronous requests are made with the .getAsync method and synchronous requests
        // are made with the .get method. This holds for all type of requests. */
        final SettableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture =
                api.authorizationCodeGrant(code).build().getAsync();

        // Add callbacks to handle success and failure
        Futures.addCallback(authorizationCodeCredentialsFuture, new FutureCallback<AuthorizationCodeCredentials>() {

            @Override
            public void onSuccess(AuthorizationCodeCredentials authorizationCodeCredentials) {
                /*
                System.out.println("ACCESS_TOKEN: " + authorizationCodeCredentials.getAccessToken());
                System.out.println("EXPIRES IN " + authorizationCodeCredentials.getExpiresIn() + " SECONDS");
                System.out.println("REFRESH_TOKEN: " + authorizationCodeCredentials.getRefreshToken());
                */
                // Set the access token and refresh token so that they are used whenever needed */
                api.setAccessToken(authorizationCodeCredentials.getAccessToken());
                api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            }
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }
        });
    }

    /**
     * Get display_name if present, otherwise the last bit of user_URI (i.e. the username)
     *
     * @return the display_name or username of the user
     */
    public String getName() {
        final CurrentUserRequest request = api.getMe().build();
        try {
            final User user = request.get();
            String name = user.getDisplayName();
            if(name == null) {
                // user_URI is of the form `spotify:user:username`
                name = user.getUri().substring(13);
            }
            return name;
        } catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
            return null;
        }
    }

    /**
     * @return the user's Spotify ID
     * @throws IOException
     * @throws WebApiException
     */
    private String getID() throws IOException, WebApiException {
        try {
            return api.getMe().build().get().getId();
        } catch(WebApiException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    /**
     * @return a list of the user's saved playlists
     * @throws IOException
     * @throws WebApiException
     */
    public List<SimplePlaylist> getSavedPlaylists() throws IOException, WebApiException {
        return api.getPlaylistsForUser(getID()).build().get().getItems();
    }

    public List<PlaylistTrack> getPlaylistTracks(SimplePlaylist playlist) throws IOException, WebApiException {
        Page<PlaylistTrack> tracks =
                api.getPlaylistTracks(playlist.getOwner().getId(), playlist.getId()).build().get();
        return tracks.getItems();
    }

    /**
     * Get Audio analysis from the tracks.
     * Exception such as status code 429: too many requests will stop the retrieval and return None for that id
     *
     * @param id the string id
     * @return either Some(AudioFeature) for the string id or None an error occurs
     */
    public Option<AudioFeature> getAnalysis(String id) {
        try {
            return Option.apply(api.getAudioFeature(id).build().get());
        } catch(Exception ex) {
            return Option.apply(null);
        }
    }

}
