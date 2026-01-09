
// AJAX Debug
const _calendar = {
    monthNames: ["Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"],
    thisYear: 6404,
    thisMonth: 10,
};
const _luna = {
    dayCount: 30,
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
            for (let d = 0; d < _luna.dayCount; d++)
                $('#grid').append('<div>' + (d + 1) + '</div>');
        /*},
    });*/
}
