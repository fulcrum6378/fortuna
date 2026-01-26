
// States
let calendar;

// Constants
const yearRange = 5;

// Calendar Setup
$.ajax({
    url: 'calendar',
    dataType: 'json',
    success: function(_calendar) {
        calendar = _calendar;

        // Month Navigation Menu
        for (m in calendar.monthNames)
            $('#nav-month').append('<span>' + calendar.monthNames[m] + '</span>');

        thisYear = calendar.thisYear;
        thisMonth = calendar.thisMonth;
        luna(thisYear, thisMonth);
    },
});

function luna(year, month) {
    //$('#nav-year').prepend('<span>6398</span>');
    // todo delete last child

    // Year Navigation Menu
    // TODO truncate #nav-year
    for (let y = 0; y < (yearRange * 2) + 1; y++) {
        let yy;
        if (y == yearRange)
            yy = calendar.thisYear;
        else if (y < yearRange)
            yy = calendar.thisYear - (yearRange - y);
        else
            yy = calendar.thisYear + (y - yearRange);
        $('#nav-year').append('<span>' + yy + '</span>');
    }

    // Month Navigation Menu
    // TODO iterate through the children and remove the `selected` attr from other items.
    $('#nav-month span:nth-child(' + month + ')').attr('selected', '');  // TODO any better method?

    $.ajax({
        url: 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        success: function(_luna) {
            for (let d = 0; d < _luna.dayCount; d++) {
                let clsMood = '';
                let clsLevel = '';
                if (_luna.scores[d] > 0) {
                    clsMood = 'pleasant ';
                    clsLevel = 'lv' + _luna.scores[d].toString().replace('.', '_');
                } else if (_luna.scores[d] < 0) {
                    clsMood = 'painful ';
                    clsLevel = 'lv' + _luna.scores[d].toString().substring(1).replace('.', '_');
                } else {
                    clsMood = 'mediocre';
                }
                $('#grid').append('<div class="dies ' + clsMood + clsLevel + '">' +
                        '<p>' + calendar.numerals[d] + '</p>' +
                        '<p>' + (_luna.scores[d] != null ? _luna.scores[d] : '?') + '</p>' + 
                        '</div>');
            }
        },
    });
}
