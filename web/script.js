
// states
let calendar = null,
    year = sessionStorage.getItem('year') ? parseInt(sessionStorage.getItem('year')) : null,
    month = sessionStorage.getItem('month') ? parseInt(sessionStorage.getItem('month')) : null,
    day = sessionStorage.getItem('day') ? parseInt(sessionStorage.getItem('day')) : null,
    defScore = null,
    lunaDayCount = null,
    orgScore = null,
    orgEmoji = null,
    orgVerbum = null,
    fetchingLuna = false,
    fetchingDies = false,
    holdingShift = false;

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
            if (fetchingLuna) return;
            month = $(this).index() + 1;
            getLuna();
        } else {
            if (fetchingDies) return;
            day = 0;
            getDies();
        }
    });

    // get the current luna
    if (year == null) year = calendar.thisYear;
    if (month == null) month = calendar.thisMonth;
    getLuna(true, true);
}

function errorAlert() {
    alert('Could not connect to the server!');
}

function getLuna(yearChanged = false, firstTime = false) {
    fetchingLuna = true;

    $('#nav-month span:nth-child(' + month + ')').addClass('pending');

    $.ajax({
        url: API_BASE_URL + 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        success: (luna) => onNewLuna(luna, yearChanged, firstTime),
        error: () => {
            fetchingLuna = false;
            errorAlert();
            $('#nav-month span:nth-child(' + month + ')').removeClass('pending');
            year = sessionStorage.getItem('year') ? parseInt(sessionStorage.getItem('year')) : null;
            month = sessionStorage.getItem('month') ? parseInt(sessionStorage.getItem('month')) : null;
        },
        timeout: API_TIMEOUT,
    });
}

function onNewLuna(luna, yearChanged, firstTime = false) {
    fetchingLuna = false;

    // store the states in the session storage
    sessionStorage.setItem('year', year);
    sessionStorage.setItem('month', month);

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
            if (fetchingLuna) return;
            let inc = $(this).index();
            if (inc < YEAR_RANGE)
                year -= YEAR_RANGE - inc;
            else
                year += inc - YEAR_RANGE;
            getLuna(true);
        });
    }

    // highlight the selected month in the month navigation menu
    $('#nav-month span:not(:nth-child(' + month + '))')
            .removeClass('pending')
            .removeAttr('selected');
    $('#nav-month span:nth-child(' + month + ')')
            .removeClass('pending')
            .attr('selected', '');

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

        let divClasses = diesClasses(
            !isFuture ? (rawScore ?? luna.defaultScore) : 0.0,
            d + 1
        ).join(' ');
        let visScore = (rawScore != null ? rawScore
                : (!isFuture && luna.defaultScore != null ? 'c. ' + luna.defaultScore
                : '?'));
        let clsScoreNonImportant = luna.scores[d] == null ? ' class="non-important"' : '';

        // DOM insertion
        $('#grid').append('<div class="dies ' + divClasses + '" title="' + luna.chronometry[d] + '">' +
                '<p>' + 
                '<span>' + (luna.emojis[d] ?? '') + '</span>' +
                (luna.verba[d] === 1 ? SVG_VERBUM : '') +
                '</p>' +
                '<p>' + (calendar.numerals != null ? calendar.numerals[d] : d + 1) + '</p>' +
                '<p' + clsScoreNonImportant + '>' + visScore + '</p>' + 
                '</div>');
    }

    // clicking on each day
    $('.dies').click(function () {
        if (fetchingDies) return;
        if (day == $(this).index() + 1) return;
        day = $(this).index() + 1;
        getDies();
    });

    // apply the default monthly details
    day = 0;
    defScore = luna.defaultScore;
    lunaDayCount = luna.dayCount;
    onNewDies({
        score: luna.defaultScore,
        emoji: luna.defaultEmoji,
        verbum: luna.defaultVerbum
    });
}

// determine proper classes for `.dies`
function diesClasses(clsScore, day_ = day) {
    let clsMood = '';
    let clsLevel = '';
    let clsToday = '';
    if (clsScore > 0) {
        clsMood = 'pleasant';
        clsLevel = 'lv' + clsScore.toString().replace('.', '_');
    } else if (clsScore < 0) {
        clsMood = 'painful';
        clsLevel = 'lv' + clsScore.toString().substring(1).replace('.', '_');
    } else {
        clsMood = 'mediocre';
    }
    if (year == calendar.thisYear && month == calendar.thisMonth &&
            day_ == calendar.thisDay)
        clsToday = 'today';

    let classes = [clsMood];
    if (clsLevel) classes.push(clsLevel);
    if (clsToday) classes.push(clsToday);
    return classes
}

function getDies() {
    fetchingDies = true;

    if (day > 0) $('.dies:nth-child(' + day + ')').addClass('pending');

    $.ajax({
        url: API_BASE_URL + 'dies?year=' + year + '&month=' + month + '&day=' + day,
        dataType: 'json',
        success: onNewDies,
        error: () => {
            fetchingDies = false;
            errorAlert();
            if (day > 0) $('.dies:nth-child(' + day + ')').removeClass('pending');
            day = sessionStorage.getItem('day') ? parseInt(sessionStorage.getItem('day')) : null;
        },
        timeout: API_TIMEOUT,
    });
}

function onNewDies(dies) {
    fetchingDies = false;

    // store the states in the session storage
    sessionStorage.setItem('day', day);

    // highlight the selected day
    $('.dies[selected]').removeAttr('selected');
    if (day > 0) $('.dies:nth-child(' + day + ')')
            .attr('selected', '')
            .removeClass('pending');

    updateDiesInGrid(dies);

    // update the panel inputs
    $('#panel > header > p > span:last-child').text(
        (day > 0) ? (DATE_SEP + (day < 10 ? '0' : '') + day) : ''
    );
    selectScore(dies.score ?? defScore);
    $('#emoji').val(dies.emoji);
    $('#verbum').val(dies.verbum);

    // update the panel buttons
    if (dies.score != null) {
        $('#save').attr('disabled', '');
        $('#clear').removeAttr('disabled');
    } else {
        if (dies.score != defScore)
            $('#save').removeAttr('disabled');
        $('#clear').attr('disabled', '');
    }
}

function updateDiesInGrid(dies) {

    // update the score in the grid cell
    if (dies.score != orgScore) {
        let isFutureLuna = year > calendar.thisYear ||
                (year == calendar.thisYear && month > calendar.thisMonth);
        let isFuture = isFutureLuna || (year == calendar.thisYear &&
                month == calendar.thisMonth && day > calendar.thisDay);
        let visScore = (dies.score != null ? dies.score
                : (!isFuture && defScore != null ? 'c. ' + defScore
                : '?'));

        // update the div classes
        let div = $('.dies:nth-child(' + day + ')');
        let oldDivClasses = diesClasses(!isFuture ? (orgScore ?? defScore) : 0.0);
        let newDivClasses = diesClasses(!isFuture ? (dies.score ?? defScore) : 0.0);
        for (const newCls of newDivClasses)
            if (!oldDivClasses.includes(newCls))
                div.addClass(newCls);
            else
                oldDivClasses.splice(oldDivClasses.indexOf(newCls), 1);
        for (const oldCls of oldDivClasses)
            div.removeClass(oldCls);

        // update the score paragraph
        let scoreParagraph = $('.dies:nth-child(' + day + ') p:nth-child(3)');
        if (dies.score == null)
            scoreParagraph.addClass('non-important');
        else
            scoreParagraph.removeClass('non-important');
        scoreParagraph.text(visScore);
    }

    // update the icons in the grid cell
    $('.dies:nth-child(' + day + ') p:first-child span').text(dies.emoji ?? '');
    if (dies.verbum != null && $('.dies:nth-child(' + day + ') p:first-child svg').length == 0)
        $('.dies:nth-child(' + day + ') p:first-child').append(SVG_VERBUM);
    else if (dies.verbum == null && $('.dies:nth-child(' + day + ') p:first-child svg').length == 1)
        $('.dies:nth-child(' + day + ') p:first-child svg').remove();

    // update the states
    orgScore = dies.score;
    orgEmoji = dies.emoji ?? '';
    orgVerbum = dies.verbum ?? '';
}

function selectScore(toScore) {
    $('#score').animate({
        scrollTop: scrollHeightOfEachScoreItem() * convertScoreToIndex(toScore)
    }, 300);
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

function differenceOfDataChangedInPanel() {
    return (selectedScore() != orgScore ? 1 : 0) +
            ($('#emoji').val() != orgEmoji ? 1 : 0) +
            (Math.abs(orgVerbum.length - $('#verbum').val().length) / 10);
}

function doPanelInputsHaveFocus() {
    return $("#panel:hover").length != 0 || $("#emoji").is(":focus") || $("#verbum").is(":focus");
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
$('#score').on('scroll', function (ev) {
    if (selectedScore() !== orgScore)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');
});
$('#emoji').on('input', function () {
    if ($(this).val() != orgEmoji)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');

    // only emojis are allowed
    if (!calendar.emojis.includes($(this).val()))
        $(this).val('');
});
$('#verbum').on('input', function () {
    if ($(this).val() != orgVerbum)
        $('#save').removeAttr('disabled');
    else if (!isDataInPanelChanged())
        $('#save').attr('disabled', '');
});

// configure the panel buttons
$('#save').click(function () {
    $('#save').attr('disabled', '');
    let newDies = {
        score: selectedScore(),
        emoji: $('#emoji').val() ? $('#emoji').val() : null,
        verbum: $('#verbum').val() ? $('#verbum').val() : null
    };

    $.ajax({
        url: API_BASE_URL + 'save?year=' + year + '&month=' + month + '&day=' + day,
        data: 'score=' + newDies.score +
                '&emoji=' + newDies.emoji +
                '&verbum=' + newDies.verbum,
        dataType: 'json',
        success: (res) => {
            if (res.status != 'ok') {
                alert('Saving failed!');  // impossible currently
                return; }

            if (day > 0)
                updateDiesInGrid(newDies);
            else
                getLuna();
            $('#clear').removeAttr('disabled');
        },
        error: () => {
            $('#save').removeAttr('disabled');
            errorAlert();
        },
        timeout: API_TIMEOUT,
    });
});
$('#reset').click(function () {
    if (differenceOfDataChangedInPanel() > 3 && !confirm('Are you sure?'))
        return;
    getDies();
});
$('#clear').click(function () {
    if ((orgEmoji != '' || orgVerbum != '') && !confirm('Are you sure?'))
        return;

    $('#save').attr('disabled', '');
    $('#clear').attr('disabled', '');

    $.ajax({
        url: API_BASE_URL + 'clear?year=' + year + '&month=' + month + '&day=' + day,
        dataType: 'json',
        success: (res) => {
            if (res.status != 'ok') {
                alert('Clearing failed!');  // impossible currently
                return; }

            if (day != 0)
                updateDiesInGrid({score: null, emoji: null, verbum: null});
            else
                getLuna();

            selectScore(day != 0 ? (defScore ?? 0.0) : 0.0);
            $('#emoji').val('');
            $('#verbum').val('');
        },
        error: () => {
            $('#clear').removeAttr('disabled');
            errorAlert();
        },
        timeout: API_TIMEOUT,
    });
});

// implement keyboard shortcuts
$(document).on('keydown', function (e) {
    switch (e.which) {

        case 16:  // SHIFT
            holdingShift = true;
            break;

        case 38:  // UP
            if (doPanelInputsHaveFocus() || fetchingLuna) return;
            let decrementor = holdingShift ? 6 : 1;
            if (month > decrementor) {
                month -= decrementor;
                getLuna();
            } else {
                decrementor -= month;
                month = calendar.monthNames.length - decrementor;
                year -= 1;
                getLuna(true);
            }
            break;

        case 40:  // DOWN
            if (doPanelInputsHaveFocus() || fetchingLuna) return;
            let incrementor = holdingShift ? 6 : 1;
            if ((month + incrementor) <= calendar.monthNames.length) {
                month += incrementor;
                getLuna();
            } else {
                incrementor -= calendar.monthNames.length - month;
                month = incrementor;
                year += 1;
                getLuna(true);
            }
            break;

        case 37:  // LEFT
            if (doPanelInputsHaveFocus() || fetchingDies || day < 1) return;
            day -= holdingShift ? 5 : 1;
            if (day < 1) day = 1;
            getDies();
            break;

        case 39:  // RIGHT
            if (doPanelInputsHaveFocus() || fetchingDies || day >= lunaDayCount) return;
            day += holdingShift ? 5 : 1;
            if (day > lunaDayCount) day = lunaDayCount;
            getDies();
            break;
    }
});
$(document).on('keyup', function (e) {
    switch (e.which) {

        case 16:  // SHIFT
            holdingShift = false;
            break;
    }
});

// implement mouse shortcuts
$('#nav-year').bind('mousewheel', function(e) {
    if (e.originalEvent.wheelDelta / 120 > 0)  // SCROLLING UP
        year--;
    else  // SCROLLING DOWN
        year++;
    getLuna(true);
});
$('#nav-month').bind('mousewheel', function(e) {
    let yearChanged = false;
    if (e.originalEvent.wheelDelta / 120 > 0) {  // SCROLLING UP
        if (month == 1) {
            month = calendar.monthNames.length;
            year--;
            yearChanged = true;
        } else
            month--;
    } else {  // SCROLLING DOWN
        if (month == calendar.monthNames.length) {
            month = 1;
            year++;
            yearChanged = true;
        } else
            month++;
    }
    getLuna(yearChanged);
});
