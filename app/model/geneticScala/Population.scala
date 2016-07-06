package model.geneticScala

import scala.util.Random

/**
  * A Population of `popSize` playlists each containing a random sequence
  * of `size` songs in the Music Collection.
  * TODO this means the MC has to be filtered before running this.
  * The collection might have duplicates, to avoid that need another function???
  * I don't think so
  */
class Population(val playlists: Vector[Playlist]) {

  // the number of the initial candidate playlists
  val popSize = playlists.length
  val size = playlists(0).size

  def get(i: Int) = playlists(i)
  def apply(c: Playlist) = playlists.find(x => x == c)

  def fittest(x: Playlist, y: Playlist): Playlist = if(x.fitness > y.fitness) x else y
  def getFittest: Playlist = playlists.reduce(fittest(_, _))
  def getFitness(c: Playlist): Float = c.fitness
  def maxFitness = getFittest.fitness

  def prettyPrint() = {
    playlists.foreach(p => {
      println("=" * 10 + '\n' + "PLAYLIST " + playlists.indexOf(p) + " (" + p.fitness + ")" + '\n' + "=" * 10)
      p.prettyPrint()
    })
  }

  /**
    * Evolution version:
    * http://www.theprojectspot.com/tutorial-post/applying-a-genetic-algorithm-to-the-travelling-salesman-problem/5
    *
    */
  def evolve: Population = {
    // https://github.com/jsvazic/GAHelloWorld/blob/master/scala/src/main/scala/net/auxesia/Population.scala
    val elites = scala.math.round(popSize * GASettings.elitismRatio)
    val eliteBuffer: Vector[Playlist] = playlists.takeRight(elites) // what for?

    /*
    println("ELITES:")
    eliteBuffer.foreach(p => p.prettyPrint())
    println("==============================")
    */

    // jsvazic uses Futures and Akka Router
    val inferiors: Array[Playlist] = (for (i <- elites until popSize) yield {
      // double check immutability with these arrays
      val darwinian = playlists(i)
      if (Random.nextFloat <= GASettings.crossoverRatio) {
        crossover(darwinian, eliteBuffer(Random.nextInt(eliteBuffer.length)))
      }
      else mutate(darwinian)
    }).toArray

    val p = new Population((eliteBuffer ++ inferiors).sortBy(p => p.fitness))
    //println("NEW POP:")
    //p.prettyPrint()
    p
  }

  // will mutate also fit ones, if over there it will test each song to be mutated:
  // that is, this is a high mutation rate algo
  def mutate(p: Playlist) = p.mutate

  /*
  private def randomMutate(p: Playlist) = {
    if (Random.nextFloat() <= GASettings.mutationRatio) p.mutate else p
  }
  */

  /**
    * Single point crossover: permutation is copied from the first parent
    * until the crossover point, then the other parent is scanned and if
    * the song is not yet in the offspring, it is added;
    * TODO this will work if the two playlists have the same size and set
    * of songs.
    */
  def crossover(p1: Playlist, p2: Playlist) = {
    if(fittest(p1, p2) == p1) p1.crossover(p2)
    else p2.crossover(p1)
  }


}