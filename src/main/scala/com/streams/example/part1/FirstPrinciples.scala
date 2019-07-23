package com.streams.example.part1

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {

  implicit val system = ActorSystem("FirstPrinciples")
  implicit val materializer = ActorMaterializer()

  // sources
  val source = Source(1 to 10)
  // sinks
  val sink = Sink.foreach[Int](println)

  val graph = source.to(sink)

  graph.run()
}
