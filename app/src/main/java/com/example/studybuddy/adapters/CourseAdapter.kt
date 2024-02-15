import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R

class CourseAdapter(private val courseList: List<SemesterCourse>) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val currentCourse = courseList[position]
        holder.bind(currentCourse)
    }

    override fun getItemCount() = courseList.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        private val courseLocationTextView: TextView = itemView.findViewById(R.id.courseLocationTextView)
        private val courseTimeTextView: TextView = itemView.findViewById(R.id.courseTimeTextView)
        private val courseDateTextView: TextView = itemView.findViewById(R.id.courseDateTextView)
        // Add other views for course details if needed

        fun bind(course: SemesterCourse) {
            courseNameTextView.text = course.name
            courseLocationTextView.text = course.location
            courseTimeTextView.text = course.time
            courseDateTextView.text = course.date
            // Bind other course details if needed
        }
    }
}
