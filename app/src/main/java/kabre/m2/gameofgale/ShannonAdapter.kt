package kabre.m2.gameofgale

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kabre.m2.gameofgale.databinding.ViewholderBottonBinding

class ShannonAdapter(
    private val board: Array<Array<String>>,
    private val context: Context,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<ShannonAdapter.BottonViewHolder>() {

    private var winningPath: Set<Pair<Int, Int>> = emptySet()  // Utiliser un Set pour une recherche plus efficace

    inner class BottonViewHolder(val binding: ViewholderBottonBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottonViewHolder {
        val binding = ViewholderBottonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottonViewHolder(binding)
    }

    override fun getItemCount(): Int = board.size * board[0].size

    override fun onBindViewHolder(holder: BottonViewHolder, position: Int) {
        val row = position / board[0].size
        val col = position % board[0].size
        holder.binding.levelNb.text = board[row][col]

        val backgroundResource = if (winningPath.contains(Pair(row, col))) {
            R.drawable.winning_background  // Arrière-plan bleu pour les cellules du chemin de victoire
        } else {
            getBackgroundResource(board[row][col])
        }
        holder.binding.levelNb.setBackgroundResource(backgroundResource)
    }

    fun updateBoard(winningPath: List<Pair<Int, Int>>) {
        this.winningPath = winningPath.toSet()  // Convertir en Set pour une recherche plus efficace
        notifyDataSetChanged()
    }

    fun getBackgroundResource(value: String): Int {
        return when (value) {
            "X" -> R.drawable.x_background
            "O" -> R.drawable.o_background
            else -> R.drawable.default_background
        }
    }
}