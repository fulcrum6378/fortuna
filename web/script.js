
// states
let calendar = null,
    year = null,
    month = null,
    day = null,
    defScore = null,
    orgScore = null,
    orgEmoji = null,
    orgVerbum = null;

// constants
const API_BASE_URL = location.protocol == 'file:' ? 'http://192.168.1.20:7007/' : '';
const API_TIMEOUT = 10000;
const YEAR_RANGE = 5;
const DATE_SEP = ' / ';
const SVG_VERBUM = '<svg><use href="#ic_verbum" /></svg>';



function onInitialise() {

    // set up the month navigation menu
    for (m in calendar.monthNames) {
        $('#nav-month').append('<span>' + calendar.monthNames[m] + '</span>');
    }
    $('#nav-month span').click(function() {
        if (month != $(this).index() + 1) {
            month = $(this).index() + 1;
            getLuna();
        } else {
            day = 0;
            getDies();
        }
    });

    // get the current luna
    year = calendar.thisYear;
    month = calendar.thisMonth;
    getLuna(true, true);
}

function errorAlert() {
    alert('Could not connect to the server!');
}

function getLuna(yearChanged = false, firstTime = false) {
    $.ajax({
        url: API_BASE_URL + 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        success: (luna) => onNewLuna(luna, yearChanged, firstTime),
        error: () => {
            errorAlert();
            // TODO fallback to previous states
        },
        timeout: API_TIMEOUT,
    });
}

function onNewLuna(luna, yearChanged, firstTime = false) {

    // update the year navigation menu
    if (yearChanged) {
        $('#nav-year').empty();
        for (let y = 0; y < (YEAR_RANGE * 2) + 1; y++) {
            let yy;
            if (y == YEAR_RANGE)
                yy = year;
            else if (y < YEAR_RANGE)
                yy = year - (YEAR_RANGE - y);
            else
                yy = year + (y - YEAR_RANGE);
            $('#nav-year').append('<span>' + yy + '</span>');
        }
        $('#nav-year span').click(function () {
            let inc = $(this).index()
            if (inc < YEAR_RANGE)
                year -= YEAR_RANGE - inc;
            else
                year += inc - YEAR_RANGE;
            getLuna(true);
        });
    }

    // highlight the selected month in the month navigation menu
    $('#nav-month span:not(:nth-child(' + month + '))').removeAttr('selected');
    $('#nav-month span:nth-child(' + month + ')').attr('selected', '');

    // empty the grid
    $('#grid').empty();

    // update the panel
    $('#panel > header > p > span:first-child').text(
        year + DATE_SEP + (month < 10 ? '0' : '') + month
    );
    if (firstTime) {
        $('#emoji, #verbum, #reset').removeAttr('disabled');
    }

    // determine if this luna is in the future
    let isFutureLuna = year > calendar.thisYear ||
            (year == calendar.thisYear && month > calendar.thisMonth);

    // loop on each day
    for (let d = 0; d < luna.dayCount; d++) {

        // essential values
        let rawScore = luna.scores[d];
        let isFuture = isFutureLuna || (year == calendar.thisYear &&
                month == calendar.thisMonth && d > calendar.thisDay - 1);

        // 'div.dies' settings
        let clsScore = rawScore ?? luna.defaultScore;
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

        // settings of the children
        let visScore = (rawScore != null ? rawScore
                : (!isFuture && luna.defaultScore != null ? 'c. ' + luna.defaultScore
                : '?'));
        let clsScoreNonImportant = luna.scores[d] == null ? ' class="non-important"' : '';

        // DOM insertion
        $('#grid').append('<div class="dies ' + clsMood + clsLevel + clsToday + '">' +
                '<p>' + 
                '<span>' + (luna.emojis[d] ?? '') + '</span>' +
                (luna.verba[d] === 1 ? SVG_VERBUM : '') +
                '</p>' +
                '<p>' + calendar.numerals[d] + '</p>' +
                '<p' + clsScoreNonImportant + '>' + visScore + '</p>' + 
                '</div>');
        // TODO other calendars in title=""
    }

    // clicking on each day
    $('.dies').click(function () {
        if (day == $(this).index() + 1) return;
        day = $(this).index() + 1;
        getDies();
    });

    // apply the default monthly details
    day = 0;
    defScore = luna.defaultScore;
    onNewDies({
        score: luna.defaultScore,
        emoji: luna.defaultEmoji,
        verbum: luna.defaultVerbum
    });
}

function getDies() {

    if (day > 0) $('.dies:nth-child(' + day + ')').addClass('pending');

    $.ajax({
        url: API_BASE_URL + 'dies?year=' + year + '&month=' + month + '&day=' + day,
        dataType: 'json',
        success: onNewDies,
        error: () => {
            errorAlert();
            if (day > 0) $('.dies:nth-child(' + day + ')')
                .attr('selected', '')
                .removeClass('pending');
            // TODO fallback to previous states
        },
        timeout: API_TIMEOUT,
    });
}

function onNewDies(dies) {

    // highlight the selected day
    $('.dies[selected]').removeAttr('selected');
    if (day > 0) $('.dies:nth-child(' + day + ')')
            .attr('selected', '')
            .removeClass('pending');

    // update the grid
    $('.dies:nth-child(' + day + ') p:first-child span').text(dies.emoji ?? '');
    if (dies.verbum != null && $('.dies:nth-child(' + day + ') p:first-child svg').length == 0)
        $('.dies:nth-child(' + day + ') p:first-child').append(SVG_VERBUM);
    else if (dies.verbum == null && $('.dies:nth-child(' + day + ') p:first-child svg').length == 1)
        $('.dies:nth-child(' + day + ') p:first-child svg').remove();

    // update the states
    orgScore = dies.score;
    orgEmoji = dies.emoji ?? '';
    orgVerbum = dies.verbum ?? '';

    // update the panel
    $('#panel > header > p > span:last-child').text(
        (day > 0) ? (DATE_SEP + (day < 10 ? '0' : '') + day) : ''
    );
    let toScore = dies.score ?? defScore;
    $('#score').animate({
        scrollTop: scrollHeightOfEachScoreItem() * convertScoreToIndex(toScore)
    }, 300);
    $('#emoji').val(dies.emoji);
    $('#verbum').val(dies.verbum);
    if (dies.score != null)
        $('#save').attr('disabled', '');
    else if (dies.score != defScore)
        $('#save').removeAttr('disabled');
}

function scrollHeightOfEachScoreItem() {
    return $('#score')[0].scrollHeight / 13;
}

function selectedScore() {
    return convertIndexToScore(
        Math.floor(($('#score').scrollTop() * 1.1) / scrollHeightOfEachScoreItem())
    );
}

function convertScoreToIndex(score) {
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
            return 6;
    }
}

function convertIndexToScore(index) {
    return [3.0, 2.5, 2.0, 1.5, 1.0, 0.5, 0.0, -0.5, -1.0, -1.5, -2.0, -2.5, -3.0][index];
}

function isDataInPanelChanged() {
    return selectedScore() != orgScore ||
            $('#emoji').val() != orgEmoji ||
            $('#verbum').val() != orgVerbum;
}



// at first...
$('#score').scrollTop(scrollHeightOfEachScoreItem() * 6);

// set up the calendar
$.ajax({
    url: API_BASE_URL + 'calendar',
    dataType: 'json',
    success: function(_calendar) {
        calendar = _calendar;
        onInitialise();
    },
    error: errorAlert,
    timeout: API_TIMEOUT,
});

// configure the panel inputs
$('#score').on('scroll', function () {
    if (selectedScore() !== orgScore)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');
});
$('#emoji').on('input', function () {
    if ($('#emoji').val() != orgEmoji)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');
});
$('#verbum').on('input', function () {
    if ($('#verbum').val() != orgVerbum)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');
});

// configure the panel buttons
$('#save').click(function () {
});
$('#reset').click(getDies);
$('#clear').click(function () {
});
