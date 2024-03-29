package com.lsc

import org.scalatest.funsuite.AnyFunSuite
import com.lsc.RandomWalk.randomWalk
import com.lsc.GraphLoader.loadGraph
import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.graphx._

// Tests
// All test cases function properly when maxNumWalks is set to at least 10
class TestCases extends AnyFunSuite {
  private val config = ConfigFactory.load()

  private val conf = new SparkConf().setAppName("GraphWalk").setMaster("local[4]")
  private val sc = new SparkContext(conf)

  private val graph = loadGraph(config.getString("Tests.FilePathOne"), sc)
  private val graph2 = loadGraph(config.getString("Tests.FilePathTwo"), sc)
  private val graph3 = loadGraph(config.getString("Tests.FilePathThree"), sc)

  graph match {
    case Some(graph) =>
      test("Simple walk with no valuable nodes") {
        val result = randomWalk (graph, graph, 1, Array.empty)
        assert(result._1 == -1 && result._2 == List.apply(1, 2, 3))
      }

      graph2 match {
        case Some(graph2) =>
          test("Simple walk with a valuable node - Expected match") {
            val nodes : Array[VertexId] = Array(3)
            val result = randomWalk (graph2, graph2, 3, nodes)
            assert (result._1 == 3 && result._2 == List.apply(3))
          }

          test("Longer walk with a valuable node - Expected Match") {
            val nodes : Array[VertexId] = Array(10)
            val result = randomWalk(graph2, graph2, 1, nodes)
            assert (result._2 == List.apply(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) && result._1 == 10)
          }

          graph3 match {
            case Some(graph3) =>
              test("Longer walk with multiple valuable nodes, the correct one should be identified") {
                val nodes: Array[VertexId] = Array(1, 10, 50)
                val result = randomWalk(graph2, graph3, 1, nodes)
                assert(result._2 == List.apply(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) && result._1 == 10)
              }

              test("Walk with valuable nodes - No match expected") {
                val nodes: Array[VertexId] = Array(1, 10, 50)
                val result = randomWalk(graph, graph3, 1, nodes)
                assert(result._2 == List.apply(1, 2, 3) && result._1 == -1)
              }

            case None =>
              throw new IllegalArgumentException("File Path or Format is Invalid.")
          }

        case None =>
          throw new IllegalArgumentException("File Path or Format is Invalid.")
      }

    case None =>
      throw new IllegalArgumentException("File Path or Format is Invalid.")
  }
}
