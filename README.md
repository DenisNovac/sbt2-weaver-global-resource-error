# sbt 2: forked tests are split across JVMs, weaver `GlobalResource` is not found

Related issue: https://github.com/typelevel/weaver-test/issues/385

Minimal reproduction. A weaver `GlobalResource` is initialised in one forked JVM, but one of the suites runs in
**another** forked JVM, so `global.getOrFailR` fails with `ResourceNotFound`.

## Setup

- sbt 2.1.0, Scala 3.3.7, JDK 25, `org.typelevel::weaver-cats:0.13.1`;
- `Test / fork := true`;
- `SharedResources` (a `GlobalResource` that puts `AppConfig`) and two identical suites `SpecA`, `SpecB`
  that read it with `global.getOrFailR[AppConfig]`

## Reproduce

```
sbt testFull
```

Result:

```
DEBUG SpecB constructed in pid=60484
DEBUG SpecA constructed in pid=60485
[info] repro.SpecB
[info] repro.SpecA
[info] - Unexpected failure 0ms
[info] *************FAILURES**************
[info] repro.SpecA
[info] + global resource is visible 3ms
[error] - Unexpected failure 0ms
[error]   GlobalResourceF$ResourceNotFound: Could not find a resource of type repro.AppConfig with label config
[error] 
[error]   GlobalResourceF.scala:156    weaver.GlobalResourceF$ResourceNotFound$#apply
[error]   GlobalResourceF.scala:108    weaver.GlobalResourceF$Read#getOrFailR$$anonfun$1
[error]   Stream.scala:4147            fs2.Stream$#resourceWeak$$anonfun$3
[error]   Stream.scala:1304            fs2.Stream#flatMap$$anonfun$1
[error]   Pull.scala:1042              fs2.Pull$FlatMapR$1#unconsed
[error]   Pull.scala:1078              fs2.Pull$FlatMapR$1#out
[error]   Pull.scala:1236              fs2.Pull$#go$1$$anonfun$1
[error]   Pull.scala:952               fs2.Pull$#interruptGuard$1$$anonfun$1
[error]   GlobalResourceF.scala:143    get @ weaver.GlobalResourceF$ResourceMap#rawGet
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Pull.scala:1214              flatMap @ fs2.Pull$#goCloseScope$1$$anonfun$1$$anonfun$3
[error]   Scope.scala:224              update @ fs2.internal.Scope#releaseChildScope
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   RunnerCompat.scala:182       tryAcquireN @ weaver.framework.RunnerCompat#run$$anonfun$1$$anonfun$2$$anonfun$1$$anonfun$1
[error]   Compiler.scala:163           flatMap @ fs2.Compiler$Target#flatMap
[error]   Compiler.scala:165           uncancelable @ fs2.Compiler$Target#uncancelable
[error]   Pull.scala:1213              flatMap @ fs2.Pull$#goCloseScope$1$$anonfun$1
[error]   Compiler.scala:161           handleErrorWith @ fs2.Compiler$Target#handleErrorWith
[error]   Pull.scala:1199              flatMap @ fs2.Pull$#goCloseScope$1
```

`SpecA` always lands in the separate JVM; `SpecB` shares the JVM with `SharedResources` and passes.

## Same project on sbt 1.11.7 (set `sbt.version=1.11.7` in `project/build.properties`)

```
DEBUG SharedResources initialised in pid=45795
DEBUG SpecB constructed in pid=45795
DEBUG SpecA constructed in pid=45795
[info] Passed: Total 2, Failed 0, Errors 0, Passed 2
```

All suites and the global resource run in one forked JVM.

## Workaround

Put all tests into one group, so that they share one forked JVM (see the commented snippet in `build.sbt`):

```scala
Test / testGrouping := Def.uncached {
  Seq(Tests.Group("all", (Test / definedTests).value, Tests.SubProcess((Test / forkOptions).value)))
}
```

