package com.github.metro

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import androidx.lifecycle.lifecycleScope
import com.github.metro.adapter.db.DataBaseAdapter
import com.github.metro.adapter.db.LocaleDatabaseProvider
import com.github.metro.constantes.ConstantesApi
import com.github.metro.constantes.ConstantesExtra
import com.github.metro.databinding.RotaPesquisaLayoutBinding
import com.github.metro.models.Linha
import com.github.metro.models.LocalPesquisa
import com.github.metro.models.Rota
import com.github.metro.recyclerViews.LinhaRvAdapter
import com.github.metro.utils.ConexaoVolley
import com.github.metro.utils.LocalizacaoUtils
import org.json.JSONException
import java.util.ArrayList
import com.github.metro.utils.converters.FavoriteLocaleConvert
import kotlinx.coroutines.launch

class RotaPesquisaActivity: ComponentActivity() {
    lateinit var binding: RotaPesquisaLayoutBinding
    lateinit var localPesquisa: LocalPesquisa

    private lateinit var rvAdapter: LinhaRvAdapter
//    val resultados = ArrayList()


    private lateinit var dataBaseAdapter: DataBaseAdapter
    private lateinit var databaseProvider: LocaleDatabaseProvider


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RotaPesquisaLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseProvider = LocaleDatabaseProvider(applicationContext)
        val favoriteLocaleDao = databaseProvider.favoriteLocaleDao
        dataBaseAdapter = DataBaseAdapter(favoriteLocaleDao)

        localPesquisa = intent.getSerializableExtra(ConstantesExtra.LOCAL_PESQUISA_EXTRA, LocalPesquisa::class.java)!!

        binding.btnFavoritar.setOnClickListener {
            lifecycleScope.launch {
                try {
                    dataBaseAdapter.insertFavoriteLocal(FavoriteLocaleConvert.toFavoriteLocal(localPesquisa))
                    Log.i("BD", "Finalizou a adição do registro.")
                } catch (e: Exception) {
                    Log.e("BD", "Erro na adição do registro: ${e}")
                }
            }
        }

        binding.tvNomeLocal.text = localPesquisa.nome
        binding.tvEnderecoLocal.text = localPesquisa.endereco
        val localOrigemPesquisa = intent.getSerializableExtra(ConstantesExtra.LOCAL_ORIGEM_PESQUISA_EXTRA, LocalPesquisa::class.java)


        if (localOrigemPesquisa != null) {
            pesquisarRota(localOrigemPesquisa)
        } else {
            val location = LocalizacaoUtils.getLocation(this)!!

            pesquisarRota(
                LocalPesquisa(
                nome = "",
                endereco = "",
                lat = location.latitude,
                lon = location.longitude
            ))
        }

        setupRecyclerViewPesquisa()

    }

    fun setupRecyclerViewPesquisa() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.rvRotas.layoutManager = layoutManager
    }


    fun mostrarRotas(rotas: ArrayList<Rota>) {
        if (rotas.size > 0 && rotas[0].linhas.size === 0) {
            binding.tvNenhumaRotaEncontrada.visibility = View.VISIBLE
            binding.rvRotas.visibility = View.GONE
            return
        }
        binding.tvNenhumaRotaEncontrada.visibility = View.GONE
        binding.rvRotas.visibility = View.VISIBLE

        rvAdapter = LinhaRvAdapter(rotas)
        binding.rvRotas.adapter = rvAdapter
    }

    fun pesquisarRota(location: LocalPesquisa) {
        val url = "${ConstantesApi.CAMINHO_LOCALIDADE}?lat=${location.lat}&lon=${location.lon}&localLat=${localPesquisa.lat}&localLon=${localPesquisa.lon}&veiculo=bus"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() < 1) {
                    // TODO: adicionar mensagem de nenhuma rota encontrada no app
                    return@JsonObjectRequest
                }

                val rotasOnibus = ArrayList<Rota>()
                for (i in 0 until routes.length()) {
                    val route = routes.getJSONObject(i)
                    try {
                        val rota = Rota(
                            linhas = ArrayList()
                        )
                        val pernas = route.getJSONArray("legs")
                        for (i in 0 until pernas.length()) {
                            val perna = pernas.getJSONObject(i)
                            val passos = perna.getJSONArray("steps")
                            for (i in 0 until passos.length()) {
                                val passo = passos.getJSONObject(i)
                                if (passo.getString("travel_mode") != "TRANSIT") {
                                    continue
                                }
                                val transitDetails =  passo.getJSONObject("transit_details")
                                val linha = transitDetails.getJSONObject("line")
                                rota.linhas.add(
                                    Linha(
                                        nome = linha.getString("name"),
                                        codigo = linha.getString("short_name")
                                    )
                                )
                            }


                        }
                        rotasOnibus.add(rota)
                    } catch(exception: JSONException) {
                        // fazer nada
                    }
                }
                mostrarRotas(rotasOnibus)
            },
            {
                    error ->
                Log.e("request erro", error.toString())
//                resultados.clear()
            })
        ConexaoVolley.getInstance(this).addToRequestQueue(request)
    }
}