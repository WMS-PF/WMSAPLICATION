package com.finalproyect.wmsaplication.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finalproyect.wmsaplication.R
import org.json.JSONArray

class ItemAdapter(val context: Context, val items: JSONArray) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Aquí se infla el layout del item y se devuelve una instancia de ViewHolder
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Aquí se establecen los valores de los elementos visuales del item
        val item = items.getJSONObject(position)
        if (position % 2 == 0){
            holder.container.setBackgroundColor(Color.parseColor("#46464F"))
        }else{
            holder.container.setBackgroundColor(Color.parseColor("#90909A"))
        }
        holder.productNombre.text = item.getString("ProductName")
        holder.IDproducto.text = item.getString("ItemCode")
        holder.productBrand.text = item.getString("Brand")
        holder.quantity.text = item.getString("Cantidad")
        // Por ejemplo: holder.title.text = items[position].title
    }

    override fun getItemCount(): Int = items.length()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val IDproducto: TextView = itemView.findViewById(R.id.productid)
        val productNombre: TextView = itemView.findViewById(R.id.Name)
        val productBrand: TextView = itemView.findViewById(R.id.brands)
        val quantity: TextView = itemView.findViewById(R.id.available)
        val container: LinearLayout = itemView.findViewById(R.id.containerItem)
        // Aquí se realizan las referencias a los elementos visuales del item
        // Por ejemplo: val title: TextView = itemView.findViewById(R.id.title)
    }
}