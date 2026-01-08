
// Year Navigation Menu
let thisYear = 6404;
for (let y = 0; y < 9; y++) {
    let year;
    if (y == 4)
        year = thisYear;
    else if (y < 4)
        year = thisYear - (4 - y);
    else
        year = thisYear + (y - 4);
    $('#nav-year').append('<span>' + year + '</span>');
}

// Month Navigation Menu
