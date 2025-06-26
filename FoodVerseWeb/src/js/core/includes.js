import $ from 'jquery'


function loadIncludes(parent) {
    if (!parent) parent = 'body'
    console.log(parent)
    $(parent).find('[wm-include]').each(function(i, e) {
        const url = $(e).attr('wm-include')

        $.ajax({
            url,
            dataType: 'html',   
            success(data) {
                $(e).html(data)
                $(e).removeAttr('wm-include')

                loadIncludes(e)
            },
            error(error) {
                console.error(`Erro ao incluir o arquivo ${url}`, error)
            }
        })
    })
}

loadIncludes()