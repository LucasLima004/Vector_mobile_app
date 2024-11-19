package com.github.metro.recyclerViews

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.metro.EstacaoActivity
import com.github.metro.constantes.ConstantesExtra
import com.github.metro.databinding.RvFavoritosBinding
import com.github.metro.databinding.RvTerminalLayoutBinding
import com.github.metro.models.FavoriteLocal

class FavoritosRvAdapter(val favoritos: List<FavoriteLocal>): RecyclerView.Adapter<FavoritosRvAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: RvFavoritosBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvFavoritosBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return favoritos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with (holder) {
            with (favoritos[position]) {
                binding.tvNomeLocal.text = this.nome
                binding.tvEnderecoLocal.text = this.endereco
            }
        }
    }

}