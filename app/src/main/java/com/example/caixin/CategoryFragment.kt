package com.example.caixin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import android.widget.ImageView
import android.widget.TextView

/**
 * 分类 Fragment
 * 原生实现的频道分类页面
 */
class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    // 频道列表回调
    interface OnChannelClickListener {
        fun onChannelClick(channel: Channel)
    }

    var onChannelClickListener: OnChannelClickListener? = null

    companion object {
        fun newInstance(): CategoryFragment {
            return CategoryFragment()
        }
    }

    data class Channel(
        val name: String,
        val url: String,
        val iconRes: Int,
        val description: String = ""
    )

    private val channels = listOf(
        Channel("首页", "https://www.caixin.com/", R.drawable.ic_home, "财新网首页"),
        Channel("经济", "https://economy.caixin.com/", R.drawable.ic_economy, "宏观经济资讯"),
        Channel("金融", "https://finance.caixin.com/", R.drawable.ic_finance, "金融市场动态"),
        Channel("公司", "https://companies.caixin.com/", R.drawable.ic_company, "企业商业新闻"),
        Channel("政经", "https://china.caixin.com/", R.drawable.ic_politics, "时政要闻"),
        Channel("国际", "https://international.caixin.com/", R.drawable.ic_world, "国际新闻"),
        Channel("观点", "https://opinion.caixin.com/", R.drawable.ic_opinion, "评论与观点"),
        Channel("文化", "https://culture.caixin.com/", R.drawable.ic_culture, "文化艺术"),
        Channel("周刊", "https://weekly.caixin.com/", R.drawable.ic_weekly, "财新周刊"),
        Channel("视频", "https://video.caixin.com/", R.drawable.ic_video, "财新视频"),
        Channel("数据", "https://database.caixin.com/", R.drawable.ic_data, "数据与图表"),
        Channel("英文", "https://www.caixinglobal.com/", R.drawable.ic_english, "Caixin Global")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            onChannelClickListener?.onChannelClick(channel)
        }
    }

    inner class ChannelAdapter(
        private val channels: List<Channel>,
        private val onClick: (Channel) -> Unit
    ) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.card)
            val icon: ImageView = view.findViewById(R.id.icon)
            val name: TextView = view.findViewById(R.id.name)
            val description: TextView = view.findViewById(R.id.description)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_channel, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val channel = channels[position]
            holder.icon.setImageResource(channel.iconRes)
            holder.name.text = channel.name
            holder.description.text = channel.description
            holder.card.setOnClickListener { onClick(channel) }
        }

        override fun getItemCount() = channels.size
    }
}
