data class StudyPlan(
    val name: String,
    val courses: Map<String, Course> = mapOf()
)
