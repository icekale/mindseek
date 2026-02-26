pluginManagement {
    repositories {
        // 阿里云镜像源
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        
        // 清华大学镜像源作为备用
        maven { url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
        
        // 原始源作为最后备用
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像源
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        
        // 清华大学镜像源作为备用
        maven { url = uri("https://mirrors.tuna.tsinghua.edu.cn/maven/") }
        
        // 原始源作为最后备用
        google()
        mavenCentral()
    }
}

rootProject.name = "XiaoyuzhouPodcastApp"
include(":app")