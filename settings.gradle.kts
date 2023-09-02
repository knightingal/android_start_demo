pluginManagement {
    repositories {
        maven {
            url=uri ("https://maven.aliyun.com/repository/public/")
        }
        maven{
            url=uri ("https://maven.aliyun.com/repository/central")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url=uri ("https://maven.aliyun.com/repository/public/")
        }
        maven{
            url=uri ("https://maven.aliyun.com/repository/central")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "flow1000-client"
include(":app" )
 