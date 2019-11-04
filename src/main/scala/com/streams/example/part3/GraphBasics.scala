package com.streams.example.part3

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}

import scala.language.postfixOps

object GraphBasics extends App {

  implicit val system = ActorSystem("GraphBasics")
  implicit val materializer = ActorMaterializer()

  val input = Source(1 to 1000)
  val increment = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_ * 10)
  val output = Sink.foreach[(Int, Int)](println)

  val graph = RunnableGraph.fromGraph(

    GraphDSL.create() {implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Int](2))
    val zip = builder.add(Zip[Int, Int])

    input ~> broadcast

    broadcast.out(0) ~> increment ~> zip.in0
    broadcast.out(1) ~> multiplier ~> zip.in1

    zip.out ~> output

    ClosedShape
  })

//  graph.run()

  /**
    * exercise 1: feed a source into 2 sinks at the same time (hint: use a broadcast)
    */

  val output1 = Sink.foreach[(Int)](println)
  val output2 = Sink.foreach[(Int)](println)


  val graph2 = RunnableGraph.fromGraph(

    GraphDSL.create() {implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[Int](2))

      input ~> broadcast

      broadcast.out(0) ~> output1
      broadcast.out(1) ~> output2

      ClosedShape
    })

//  graph2.run()

  /**
    * exercise 2: balance
    */


  import scala.concurrent.duration._
  val fastSource = Source(1 to 1000).throttle(5, 1 second)
  val slowSource = Source(1000 to 2000).throttle(2, 1 second)

  val output12 = Sink.foreach[(Int)](r => println(s"Result 1: $r"))
  val output22 = Sink.foreach[(Int)](r => println(s"Result 2: $r"))

  val graph3 = RunnableGraph.fromGraph(

    GraphDSL.create() {implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[Int](2))
      val balance = builder.add(Balance[Int](2))

      slowSource ~> merge
      fastSource ~> merge

      merge ~> balance

      balance ~> output12
      balance ~> output22


      ClosedShape
    })

    graph3.run()

}
