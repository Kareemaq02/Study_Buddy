
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.data.teachRequest

class TeachRequestAdapter(private val context: Context, private val teachRequests: List<teachRequest>) : RecyclerView.Adapter<TeachRequestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_teach_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val teachRequest = teachRequests[position]
        holder.bind(teachRequest)
    }

    override fun getItemCount(): Int {
        return teachRequests.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val majorTextView: TextView = itemView.findViewById(R.id.textViewMajor)
        private val courseTextView: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewTime)
        private val placeTextView: TextView = itemView.findViewById(R.id.textViewPlace)
        private val participantsTextView: TextView = itemView.findViewById(R.id.textViewParticipantsCount)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)

        fun bind(teachRequest: teachRequest) {
            majorTextView.text = teachRequest.major_Name
            courseTextView.text = teachRequest.course_Name
            dateTextView.text = teachRequest.req_date
            timeTextView.text = "${teachRequest.start_time} - ${teachRequest.end_time}"
            placeTextView.text = teachRequest.place
            participantsTextView.text = teachRequest.number_of_students.toString()
            descriptionTextView.text = teachRequest.lessonDescription
        }
    }
}
