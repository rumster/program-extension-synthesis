clock(mut days:int) -> (year:int) {
  year = 1980;
  while (days > 365) {
    if (year == 2008) {
      if (days > 366) {
        days = days - 366;
        year = year + 1;
      }
    }
    else {
      days = days - 365;
      year = year + 1;
    }
  }

  example {
    [days==10220] -> ...
  }

  example {
    [days==366] -> ...
  }

  example {
    [days==10221] -> ...
  }

  example {
    [days==11950] -> ...
  }
}