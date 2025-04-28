pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        // 国内镜像
        maven(url = "https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 国内镜像
        maven(url = "https://maven.aliyun.com/repository/public")
    }
}

rootProject.name = "PDR_Locator"
include(":app")
 