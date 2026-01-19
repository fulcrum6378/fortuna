
// AJAX Debug
const _calendar = {
    monthNames: ['Farvardin', 'Ordibehesht', 'Khordad', 'Tir', 'Mordad', 'Shahrivar',
                 'Mehr', 'Aban', 'Azar', 'Dey', 'Bahman', 'Esfand'],
    dayNumerals: ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X',
                  'XI', 'XII', 'XIII', 'XIV', 'XV', 'XVI', 'XVII', 'XVIII', 'XIX', 'XX',
                  'XXI', 'XXII', 'XXIII', 'XXIV', 'XXV', 'XXVI', 'XXVII', 'XXVIII', 'XXIX', 'XXX'],
    thisYear: 6404,
    thisMonth: 10,
};
const _luna = {
    dayCount: 30,
    scores: [0.5, 1, 2, 1, 3, 0, -0.5, -1, -1.5, -2,
             -2.5, 0, -3, 2.5, 2, 1.5, 1, 0.5, 0, 3,
             0, 1, 1, 1, 1, 1, 1.5, 1, 2, 3],
    emojis: [],
    verba: [],
};

// States
let thisYear;
let thisMonth;

// Constants
const yearRange = 5;

// Calendar Setup
/*$.ajax({
    url: 'calendar',
    dataType: 'json',
    async: false,
    success: function(_calendar) {*/
        // Month Navigation Menu
        for (m in _calendar.monthNames)
            $('#nav-month').append('<span>' + _calendar.monthNames[m] + '</span>');
        
        thisYear = _calendar.thisYear;
        thisMonth = _calendar.thisMonth;
        luna(thisYear, thisMonth);
    /*},
});*/

function luna(year, month) {
    //$('#nav-year').prepend('<span>6398</span>');
    // todo delete last child
    
    // Year Navigation Menu
    // TODO truncate #nav-year
    for (let y = 0; y < (yearRange * 2) + 1; y++) {
        let yy;
        if (y == yearRange)
            yy = thisYear;
        else if (y < yearRange)
            yy = thisYear - (yearRange - y);
        else
            yy = thisYear + (y - yearRange);
        $('#nav-year').append('<span>' + yy + '</span>');
    }
    
    // Month Navigation Menu
    // TODO iterate through the children and remove the `selected` attr from other items.
    $('#nav-month span:nth-child(' + month + ')').attr('selected', '');  // TODO any better method?
    
    /*$.ajax({
        url: 'luna?year=' + year + '&month=' + month,
        dataType: 'json',
        async: false,
        success: function(_luna) {*/
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
                        '<p>' + _calendar.dayNumerals[d] + '</p>' +
                        '<p>' + _luna.scores[d] + '</p>' + 
                        '</div>');
            }
        /*},
    });*/
}
