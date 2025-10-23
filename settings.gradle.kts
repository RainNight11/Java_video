pluginManagement {
    repositories {
        // Android 相关插件
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Gradle 插件门户与中央仓库
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 严格禁止模块内声明仓库（保留这一条没问题，@Incubating 警告可以忽略）
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        // 如需第三方/私有源可在此追加：
        // maven("https://jitpack.io")
        // maven("https://your.company.repo") { name = "CompanyRepo" }
    }
}

rootProject.name = "Java_video"

// 正确：使用位置参数，一次性列出
include(":app", ":backend")

// ⚠️ 注意：不要 include 非 Gradle 项目（如纯前端的 web-dashboard）
// 如果只是前端资源，不要 include(":web-dashboard")
