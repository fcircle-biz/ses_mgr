import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test

class TestSingleTask extends DefaultTask {
    String testClassName

    @TaskAction
    def runTest() {
        project.tasks.register('testSingle', Test) {
            useJUnitPlatform()
            filter {
                includeTestsMatching testClassName
            }
        }
        project.tasks.testSingle.execute()
    }
}