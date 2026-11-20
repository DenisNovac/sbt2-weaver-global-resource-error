package repro

import cats.effect.{IO, Resource}
import weaver.*

final case class AppConfig(value: String)

object SharedResources extends GlobalResource with LowPriorityImplicits {
  val ConfigLabel: Option[String] = Some("config")

  override def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      _ <- Resource.eval(IO.println(s"DEBUG SharedResources initialised in pid=${ProcessHandle.current.pid}"))
      _ <- global.putR(AppConfig("hello"), label = ConfigLabel)(using classBasedInstance)
    } yield ()
}
