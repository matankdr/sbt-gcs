/*
 * Copyright 2018 saint1991
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.saint1991.sbt.gcs

import java.util.concurrent.atomic.AtomicLongArray

import scala.concurrent.Future

import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.reactive.Observer
import sbt.util.Logger


object ProgressBar {
  private final val OpenBracket = "["
  private final val CloseBracket = "]"
  private final val Bar = "="
  private final val Space = " "
  private final val Top = ">"
  private final val Width = 50
}

/**
  * Progress bar indicator for asynchronous multi tasks.
  * @param maxValues the pairs of (object name, object size)
  */
class ProgressBar(private [gcs] val maxValues: Seq[(String, Long)]) {
  import ProgressBar._

  private final val length = maxValues.length
  private final val numOfDigits = maxValues.map(_._2).max.toString.length
  private final val progressFormat = s"%${numOfDigits}d"

  private val progresses = new AtomicLongArray(length)

  protected def print(str: String): Unit = Console.print(str)

  def setProgress(index: Int, progress: Long): Unit = progresses.set(index, progress)

  def initRendered: String = "\n" * length
  def initRender(): Unit = print(initRendered)

  def rendered: String = maxValues.zipWithIndex.foldLeft(new StringBuilder(s"\u001b[${length}A")) {
    case (builder, ((name, max), index)) => builder.append(createLine(progresses.get(index), max, name))
  }.toString()

  def render(): Unit = print(rendered)

  private def createLine(progress: Long, max: Long, name: String): String = {

    val p = if (progress > max) max else progress
    val barLen = (Width / max.toDouble * p).toInt
    val spaceLen = Width - barLen

    val line = new StringBuilder(OpenBracket)
    for (_ <- 0L until (barLen - 1)) line.append(Bar)
    line.append(
      if (barLen == 0) ""
      else if (progress >= max) Bar
      else Top
    )
    for (_ <- 0L until spaceLen) line.append(Space)
    line.append(CloseBracket)
    line.append("\u0009")

    line.append(s"( ${progressFormat format p} / ${progressFormat format max} )")
    line.append("\u0009")
    line.append(name)
    line.append("\n")

    line.toString
  }
}

/**
  * Progress bar indicator for asynchronous multi tasks.
  * Rendering via the given Logger.
  * @param maxValues the pairs of (object name, object size)
  * @param logger sbt logger
  */
class LoggingProgressBar(maxValues: Seq[(String, Long)], logger: Logger) extends ProgressBar(maxValues) {
  override def print(str: String): Unit = logger.info(str)
}

/**
  * Observer of InputStream that indicates the progress of a certain task identified by *taskIndex*.
  * @param taskIndex index of corresponding task in *maxValues* passed to ProgressBar
  * @param progressBar ProgressBar instance
  */
class ProgressObserver(taskIndex: TaskIndex, progressBar: ProgressBar) extends Observer[Array[Byte]] {

  private var accSize: Long = 0L

  override def onError(ex: Throwable): Unit = throw ex
  override def onComplete(): Unit = {
    progressBar.setProgress(taskIndex, progressBar.maxValues(taskIndex)._2)
    progressBar.render()
  }

  override def onNext(elem: Array[Byte]): Future[Ack] = {
    accSize += elem.length
    progressBar.setProgress(taskIndex, accSize)
    progressBar.render()
    Continue
  }
}
