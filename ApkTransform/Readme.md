"instrumentor" directory is a standalone gradle plugin, which turns into a maven local repository via uploadArchive task (though, it needs configuruing in build.gradle as in latter)
Testapp is a simple android studio project with applied 'instrumentor' plugin 
(That's not for sure, but) you may sample transformation with :app->tasks->other->assembleDebug (at least, this task should provide it?)
Since we didn't use -monkey or monkeyrunner or testing stuff, it hardly can be inspected what has changed, so you can try observing bytecode in build directory or work with .apk file or anything.

 