package com.egon12.developerhelper.rest

import androidx.recyclerview.widget.DiffUtil
import com.egon12.developerhelper.rest.persistent.HttpRequest

class RequestDiffUtil: DiffUtil.ItemCallback<HttpRequest>()  {

    override fun areItemsTheSame(o: HttpRequest, n: HttpRequest) = o.id == n.id

    override fun areContentsTheSame(o: HttpRequest, n: HttpRequest) = o == n

}
