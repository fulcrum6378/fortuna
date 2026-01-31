
// States
let calendar = null;
let year = null;
let month = null;
let dies = null;

// Constants
const yearRange = 5;

// Calendar Setup
$.ajax({
    url: 'calendar',
    dataType: 'json',
    success: function(_calendar) {
        calendar = _calendar;

        // Month Navigation Menu
        for (m in calendar.monthNames) {
            $('#nav-month').append('<span>' + calendar.monthNames[m] + '</span>');
        }
        $('#nav-month span').click(function() {
            if (month == $(this).index() + 1) return;
            month = $(this).index() + 1;
            luna(false);
        });

        year = calendar.thisYear;
        month = calendar.thisMonth;
        luna(true);
    },
});

function luna(yearChanged) {

    // Year Navigation Menu
    if (yearChanged) {
        $('#nav-year').empty();
        for (let y = 0; y < (yearRange * 2) + 1; y++) {
            let yy;
            if (y == yearRange)
                yy = year;
            else if (y < yearRange)
                yy = year - (yearRange - y);
            else
                yy = year + (y - yearRange);
            $('#nav-year').append('<span>' + yy + '</span>');
        }
        $('#nav-year span').click(function () {
            let inc = $(this).index()
            if (inc < yearRange)
                year -= yearRange - inc;
            else
                year += inc - yearRange;
            luna(true);
        });
    }

    // Month Navigation Menu
    $('#nav-month span:not(:nth-child(' + month + '))').removeAttr('selected');
    $('#nav-month span:nth-child(' + month + ')').attr('selected', '');

    $.ajax({
        url: 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        success: function(_luna) {
            $('#grid').empty();
            for (let d = 0; d < _luna.dayCount; d++) {
                let score = _luna.scores[d] != null ? _luna.scores[d] : _luna.defaultScore;
                let clsMood = '';
                let clsLevel = '';
                if (score > 0) {
                    clsMood = 'pleasant ';
                    clsLevel = 'lv' + score.toString().replace('.', '_');
                } else if (score < 0) {
                    clsMood = 'painful ';
                    clsLevel = 'lv' + score.toString().substring(1).replace('.', '_');
                } else {
                    clsMood = 'mediocre';
                }
                $('#grid').append('<div class="dies ' + clsMood + clsLevel + '">' +
                        '<p>' + 
                        '<span>' + (_luna.emojis[d] ? _luna.emojis[d] : '') + '</span>' +
                        (_luna.verba[d] === 1 ? '<svg><use href="#verbum" /></svg>' : '') +
                        '</p>' +
                        '<p>' + calendar.numerals[d] + '</p>' +
                        '<p>' + (_luna.scores[d] != null ? _luna.scores[d]
                                : (_luna.defaultScore != null ? "c. " + _luna.defaultScore
                                : '?')) + '</p>' + 
                        '</div>');
            }
        },
    });
}
