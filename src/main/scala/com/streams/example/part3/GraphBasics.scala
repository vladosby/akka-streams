package com.streams.example.part3

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, Zip}

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

  graph2.run()
}
