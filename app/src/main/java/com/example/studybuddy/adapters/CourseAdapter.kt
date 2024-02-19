import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R

class CourseAdapter(private val courseList: MutableList<SemesterCourse>, private val onDeleteLongClickListener: OnDeleteLongClickListener) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    interface OnDeleteLongClickListener {
        fun onDeleteLongClick(course: SemesterCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val currentCourse = courseList[position]
        holder.bind(currentCourse)
    }

    override fun getItemCount() = courseList.size

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        private val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        private val courseLocationTextView: TextView = itemView.findViewById(R.id.courseLocationTextView)
        private val courseTimeTextView: TextView = itemView.findViewById(R.id.courseTimeTextView)
        private val courseDateTextView: TextView = itemView.findViewById(R.id.courseDateTextView)

        init {
            // Set long click listener for the item
            itemView.setOnLongClickListener(this)
        }

        fun bind(course: SemesterCourse) {
            courseNameTextView.text = course.name
            courseLocationTextView.text = course.location
            courseTimeTextView.text = course.time
            courseDateTextView.text = course.date
        }

        override fun onLongClick(v: View): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val course = courseList[position]
                onDeleteLongClickListener.onDeleteLongClick(course)
                return true
            }
            return false
        }
    }
}
