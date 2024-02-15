package com.example.studybuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.data.LearnRequestItem

class LearnRequestAdapter(
    private val learnRequestItems: List<LearnRequestItem>,
    private val onUpvoteClickListener: (Int) -> Unit
) : RecyclerView.Adapter<LearnRequestAdapter.LearnRequestViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnRequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learn_request, parent, false)
        return LearnRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: LearnRequestViewHolder, position: Int) {
        val learnRequestItem = learnRequestItems[position]
        holder.bind(learnRequestItem)
    }

    override fun getItemCount(): Int {
        return learnRequestItems.size
    }

    inner class LearnRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firstNameTextView: TextView = itemView.findViewById(R.id.textViewFirstName)
        private val lastNameTextView: TextView = itemView.findViewById(R.id.textViewLastName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
        private val majorTextView: TextView = itemView.findViewById(R.id.textViewMajor)
        private val courseNameTextView: TextView = itemView.findViewById(R.id.textViewCourseName)
        private val preferredTimeTextView: TextView = itemView.findViewById(R.id.textViewPreferredTime)
        private val upvoteButton: Button = itemView.findViewById(R.id.buttonUpvote)
        private val upvoteCount: TextView = itemView.findViewById(R.id.textViewUpvotesCount)
        private var isUpvoted: Boolean = false

        init {
            upvoteButton.setOnClickListener {
                isUpvoted = !isUpvoted
                onUpvoteClickListener.invoke(adapterPosition)
            }
        }

        fun bind(learnRequestItem: LearnRequestItem) {
            firstNameTextView.text = learnRequestItem.firstName
            lastNameTextView.text = learnRequestItem.lastName
            descriptionTextView.text = learnRequestItem.description
            majorTextView.text = learnRequestItem.major
            courseNameTextView.text = learnRequestItem.courseName
            preferredTimeTextView.text = learnRequestItem.preferredTime
            upvoteCount.text = learnRequestItem.upvotersCount.toString()
        }
    }
}
