import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R

class UserScheduleAdapter(
    private val semesterList: List<String>,
    private val onItemClickListener: OnItemClickListener,
    private val onItemLongClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<UserScheduleAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(semester: String)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(semester: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_semester, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val semesterName = semesterList[position]
        holder.bind(semesterName)
    }

    override fun getItemCount(): Int {
        return semesterList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val semesterTextView: TextView = itemView.findViewById(R.id.semesterNumberTextView)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun bind(semesterName: String) {
            semesterTextView.text = semesterName
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val semester = semesterList[position]
                onItemClickListener.onItemClick(semester)
            }
        }

        override fun onLongClick(v: View): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val semester = semesterList[position]
                onItemLongClickListener.onItemLongClick(semester)
                return true
            }
            return false
        }
    }
}
