package com.constructionmanagement.app.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Payment
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.util.CurrencyFormatter
import com.google.android.material.chip.Chip
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class PaymentReportAdapter :
    ListAdapter<Payment, PaymentReportAdapter.PaymentReportViewHolder>(PaymentReportDiffCallback()) {

    // Map to cache worker data for better performance
    private val workerMap = mutableMapOf<Long, Worker>()
    private val siteNameMap = mutableMapOf<Long, String>()

    fun setWorkerData(workers: List<Worker>) {
        workerMap.clear()
        workers.forEach { worker ->
            workerMap[worker.id] = worker
        }
        notifyDataSetChanged()
    }

    fun setSiteData(siteMap: Map<Long, String>) {
        siteNameMap.clear()
        siteNameMap.putAll(siteMap)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_report, parent, false)
        return PaymentReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentReportViewHolder, position: Int) {
        val payment = getItem(position)
        holder.bind(payment, workerMap[payment.workerId], siteNameMap[payment.siteId])
    }

    class PaymentReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageWorker: CircleImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textSiteName: TextView = itemView.findViewById(R.id.text_site_name)
        private val textAmount: TextView = itemView.findViewById(R.id.text_amount)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val chipPaymentMode: Chip = itemView.findViewById(R.id.chip_payment_mode)

        fun bind(payment: Payment, worker: Worker?, siteName: String?) {
            // Set worker name
            textWorkerName.text = worker?.name ?: "Unknown Worker"

            // Set site name
            textSiteName.text = siteName ?: "Site ID: ${payment.siteId}"

            textAmount.text = CurrencyFormatter.formatRupees(payment.amount)
            textDate.text = payment.paymentDate
            textDescription.text = payment.description
            chipPaymentMode.text = payment.paymentMode.name

            // Load worker image if available
            if (worker?.profileImagePath != null) {
                val imageFile = File(worker.profileImagePath)
                if (imageFile.exists()) {
                    Glide.with(itemView.context)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imageWorker)
                } else {
                    imageWorker.setImageResource(R.drawable.ic_person)
                }
            } else {
                imageWorker.setImageResource(R.drawable.ic_person)
            }
        }
    }

    class PaymentReportDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.paymentId == newItem.paymentId
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}
