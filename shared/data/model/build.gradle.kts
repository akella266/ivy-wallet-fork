plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(libs.kotlin.datetime)
}