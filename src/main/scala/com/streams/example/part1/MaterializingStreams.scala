package com.streams.example.part1

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {

  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
//  val simpleMaterializedValue = simpleGraph.run()

  // choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)
  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph.run().onComplete {
    case Success(_) => println("Stream processing finished.")
    case Failure(exception) => println(s"Stream processing failed with: $exception")
  }

  // sugars
  Source(1 to 10).runWith(Sink.reduce[Int](_ + _)) // source.to(Sink.reduce)(Keep.right)
  val result = Source(1 to 10).runReduce[Int](_ + _) // same
  result.map(r => println(s"result = $r"))

  // backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) // source(..).to(sink...).run()
  // both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
    * - return the last element out of a source (use Sink.last)
    * - compute the total word count out of a stream of sentences
    *   - map, fold, reduce
    */

  Source(1 to 10).runWith(Sink.last).map(println)
  Source(1 to 10).toMat(Sink.last)(Keep.right).run().map(println)
//  Source(1 to 10).to(Sink.last).run().map(println) // does not work

  val sentenceSource = Source(List(
    "Akka is awesome",
    "I love streams",
    "Materialized values are killing me"
  ))

  sentenceSource.runWith(Sink.fold(0)((acc, value) => acc + value.split(" ").length)).map(println)

  sentenceSource.viaMat(Flow[String].fold[Int](0)((acc, value) => acc + value.split(" ").length))(Keep.right)
    .toMat(Sink.head)(Keep.right).run().map(println)

  val wordCountSink = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2

}

