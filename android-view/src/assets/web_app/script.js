
// States
let calendar = null;
let year = null;
let month = null;
let day = null;

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
            get_luna(false);
        });

        year = calendar.thisYear;
        month = calendar.thisMonth;
        get_luna(true);
    },
});

function get_luna(yearChanged) {

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
            get_luna(true);
        });
    }

    // Month Navigation Menu
    $('#nav-month span:not(:nth-child(' + month + '))').removeAttr('selected');
    $('#nav-month span:nth-child(' + month + ')').prop('selected', true);

    $.ajax({
        url: 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        success: function(_luna) {
            $('#grid').empty();
            let isFutureLuna = year > calendar.thisYear ||
                    (year == calendar.thisYear && month > calendar.thisMonth);

            for (let d = 0; d < _luna.dayCount; d++) {
                let rawScore = _luna.scores[d];
                let isFuture = isFutureLuna || (year == calendar.thisYear &&
                        month == calendar.thisMonth && d > calendar.thisDay - 1);

                let clsScore = rawScore != null ? rawScore : _luna.defaultScore;
                let clsMood = '';
                let clsLevel = '';
                let clsToday = '';
                if (clsScore > 0) {
                    clsMood = 'pleasant ';
                    clsLevel = 'lv' + clsScore.toString().replace('.', '_') + ' ';
                } else if (clsScore < 0) {
                    clsMood = 'painful ';
                    clsLevel = 'lv' + clsScore.toString().substring(1).replace('.', '_') + ' ';
                } else {
                    clsMood = 'mediocre ';
                }
                if (year == calendar.thisYear && month == calendar.thisMonth &&
                        d == calendar.thisDay - 1)
                    clsToday = 'today ';

                let visScore = (rawScore != null ? rawScore
                        : (!isFuture && _luna.defaultScore != null ? 'c. ' + _luna.defaultScore
                        : '?'));
                let clsScoreNonImportant = _luna.scores[d] == null ? ' class="non-important"' : '';

                $('#grid').append('<div class="dies ' + clsMood + clsLevel + clsToday + '">' +
                        '<p>' + 
                        '<span>' + (_luna.emojis[d] ? _luna.emojis[d] : '') + '</span>' +
                        (_luna.verba[d] === 1 ? '<svg><use href="#ic_verbum" /></svg>' : '') +
                        '</p>' +
                        '<p>' + calendar.numerals[d] + '</p>' +
                        '<p' + clsScoreNonImportant + '>' + visScore + '</p>' + 
                        '</div>');
            }

            $('.dies').click(function () {
                if (day == $(this).index() + 1) return;
                day = $(this).index() + 1;
                dies();
            });

            day = 0;
            /*$('#score option:eq(' + scoreToIndex(_luna.defaultScore) + ')').prop('selected', true);
            $('#emoji').val(_luna.defaultEmoji);
            $('#verbum').val(_luna.defaultVerbum);*/
            dies_success({score: _luna.defaultScore, emoji: _luna.defaultEmoji, verbum: defaultVerbum});
        },
    });
}

function dies() {
    $('.dies[selected]').removeAttr('selected');
    if (day > 0) $('.dies:nth-child(' + day + ')').prop('selected', true);

    $.ajax({
        url: 'dies?year=' + year + '&month=' + month + '&day=' + day,
        dataType: 'json',
        success: dies_success,
    });
}

function dies_success(_dies) {
    $('#score option:eq(' + scoreToIndex(_dies.score) + ')').prop('selected', true);
    $('#emoji').val(_dies.emoji);
    $('#verbum').val(_dies.verbum);
}

function scoreToIndex(score) {
    switch (score) {
        case 3.0:
            return 0;
        case 2.5:
            return 1;
        case 2.0:
            return 2;
        case 1.5:
            return 3;
        case 1.0:
            return 4;
        case 0.5:
            return 5;
        case 0.0:
            return 6;
        case -0.5:
            return 7;
        case -1.0:
            return 8;
        case -1.5:
            return 9;
        case -2.0:
            return 10;
        case -2.5:
            return 11;
        case -3.0:
            return 12;
        default:
            return 5;
    }
}
