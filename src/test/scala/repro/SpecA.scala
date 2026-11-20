package repro

import cats.effect.IO
import weaver.*

class SpecA(global: GlobalRead) extends IOSuite with LowPriorityImplicits {
  println(s"DEBUG SpecA constructed in pid=${ProcessHandle.current.pid}")

  override type Res = AppConfig

  override def sharedResource: cats.effect.Resource[IO, AppConfig] =
    global.getOrFailR[AppConfig](SharedResources.ConfigLabel)(using classBasedInstance)

  test("global resource is visible") { config =>
    IO(expect(config == AppConfig("hello")))
  }
}
