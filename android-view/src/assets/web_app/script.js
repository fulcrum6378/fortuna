const base_url = "http://192.168.1.20:7007/";
//const base_url = "/";

let thisYear = 6404;
let thisMonth = 10;
function luna(year, month) {
    $('#nav-year').prepend('<span>6398</span>');
    // todo delete last child
}

// Year Navigation Menu
const yearRange = 5;
for (let y = 0; y < (yearRange * 2) + 1; y++) {
    let year;
    if (y == yearRange)
        year = thisYear;
    else if (y < yearRange)
        year = thisYear - (yearRange - y);
    else
        year = thisYear + (y - yearRange);
    $('#nav-year').append('<span>' + year + '</span>');
}

// Month Navigation Menu
$.ajax({
    url: base_url + 'month_names',
    dataType: 'json',
    async: true,
    success: function(monthNames) {
        for (m in monthNames) {
            $('#nav-month').append('<span' + ((parseInt(m) + 1 == thisMonth) ? ' selected' : '') + '>' + 
                    monthNames[m] + '</span>');
        }
    },
});
