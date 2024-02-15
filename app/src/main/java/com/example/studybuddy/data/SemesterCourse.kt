import java.io.Serializable

data class SemesterCourse(
    val name: String,
    val location: String,
    val time: String,
    var date: String,
) : Serializable {
    // ...
}
