import java.io.Serializable

data class Course(
    val name: String,
    val code: String,
    val description: String,
    var grade: String,
    val prerequisites: ArrayList<String>?
) : Serializable {
    // ...
}
