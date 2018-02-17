create table db_english_grids
(
  id int auto_increment
    primary key,
  grid varchar(30) not null,
  playername varchar(16) null,
  opentime date null,
  isfinished int default '0' null,
  constraint db_english_grids_grid_pk
  unique (grid)
)
;

create table db_english_words
(
  id int auto_increment
    primary key,
  word varchar(64) null,
  length int not null,
  number_of_A int not null,
  number_of_B int not null,
  number_of_C int not null,
  number_of_D int not null,
  number_of_E int not null,
  number_of_F int not null,
  number_of_G int not null,
  number_of_H int not null,
  number_of_I int not null,
  number_of_J int not null,
  number_of_K int not null,
  number_of_L int not null,
  number_of_M int not null,
  number_of_N int not null,
  number_of_O int not null,
  number_of_P int not null,
  number_of_Q int not null,
  number_of_R int not null,
  number_of_S int not null,
  number_of_T int not null,
  number_of_U int not null,
  number_of_V int not null,
  number_of_W int not null,
  number_of_X int not null,
  number_of_Y int not null,
  number_of_Z int not null,
  valid int(1) default '1' null
)
;

