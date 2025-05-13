package com.constructionmanagement.app.ui.site

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Site
import com.constructionmanagement.app.data.model.SiteStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class SiteAdapter(private val onSiteClicked: (Long) -> Unit) :
    ListAdapter<Site, SiteAdapter.SiteViewHolder>(SiteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_site, parent, false)
        return SiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val site = getItem(position)
        holder.bind(site, onSiteClicked)
    }

    class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSiteSN: TextView = itemView.findViewById(R.id.text_site_sn)
        private val textSiteName: TextView = itemView.findViewById(R.id.text_site_name)
        private val textSiteAddress: TextView = itemView.findViewById(R.id.text_site_address)
        private val textSiteClient: TextView = itemView.findViewById(R.id.text_site_client)
        private val textSiteClientContact: TextView = itemView.findViewById(R.id.text_site_client_contact)
        private val textSiteStartDate: TextView = itemView.findViewById(R.id.text_site_start_date)
        private val chipSiteStatus: Chip = itemView.findViewById(R.id.chip_site_status)
        private val buttonViewSite: MaterialButton = itemView.findViewById(R.id.button_view_site)

        fun bind(site: Site, onSiteClicked: (Long) -> Unit) {
            // Set serial number (position + 1)
            textSiteSN.text = (adapterPosition + 1).toString()
            textSiteName.text = site.name
            textSiteAddress.text = site.address
            textSiteClient.text = site.clientName
            textSiteClientContact.text = site.clientContact
            textSiteStartDate.text = site.startDate

            // Set site status chip
            chipSiteStatus.text = site.status.name
            val statusColor = when (site.status) {
                SiteStatus.ACTIVE -> R.color.success
                SiteStatus.COMPLETED -> R.color.completed
                SiteStatus.ON_HOLD -> R.color.warning
            }
            chipSiteStatus.setChipBackgroundColorResource(statusColor)

            // Set view button click listener
            buttonViewSite.setOnClickListener {
                onSiteClicked(site.siteId)
            }
        }
    }

    class SiteDiffCallback : DiffUtil.ItemCallback<Site>() {
        override fun areItemsTheSame(oldItem: Site, newItem: Site): Boolean {
            return oldItem.siteId == newItem.siteId
        }

        override fun areContentsTheSame(oldItem: Site, newItem: Site): Boolean {
            return oldItem == newItem
        }
    }
}
