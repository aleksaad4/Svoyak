'use strict';

var gulp = require('gulp'),
    prefix = require('gulp-autoprefixer'),
    less = require('gulp-less'),
    minifyCss = require('gulp-minify-css'),
    minifyHtml = require('gulp-minify-html'),

    usemin = require('gulp-usemin'),
    uglify = require('gulp-uglify'),
    imagemin = require('gulp-imagemin'),
    order = require("gulp-order"),
    templateCache = require('gulp-angular-templatecache'),
    ngAnnotate = require('gulp-ng-annotate'),
    jshint = require('gulp-jshint'),
    rev = require('gulp-rev'),
    es = require('event-stream'),
    concat = require('gulp-concat'),
    flatten = require('gulp-flatten'),
    clean = require('gulp-clean'),
    replace = require('gulp-replace'),
    sourcemaps = require('gulp-sourcemaps'),
    rigger = require('gulp-rigger'),

    rework = require('gulp-rework'),
    reworkUrl = require('rework-plugin-url');

var tvguide = {
    app: 'src/main/webapp/',
    dist: 'target/webapp_dist/',
    test: 'src/test/javascript/spec/',
    tmp: 'target/tmp/'
};

var exec = require('child_process').exec;

gulp.task('bower-installer', function (cb) {
    exec('bower-installer', function (err, stdout, stderr) {
        console.log(stdout);
        console.log(stderr);
        cb(err);
    });
});

gulp.task('clean', function () {
    return gulp.src(tvguide.dist, {read: false}).pipe(clean());
});

// сборка less-файлов
gulp.task('less', function () {
    gulp.src(tvguide.app + 'external/**/less/*.less').pipe(less()).pipe(gulp.dest(tvguide.app + 'css/external/'));
    return gulp.src(tvguide.app + 'less/**/*.less').pipe(less()).pipe(gulp.dest(tvguide.app + 'css/'));
});

// дальше нужно склеить css-файлы в 1 для маленькой версии  и в 2 - для большой
gulp.task('styles', ['less'], function () {
    // CSS для маленькой версии
    gulp.src(tvguide.app + 'css/**/small/*.css').pipe(concat('small.min.css')).pipe(minifyCss()).pipe(gulp.dest(tvguide.app + 'css/'));

    // CSS для большой версии
    gulp.src(tvguide.app + 'css/**/big/*.css').pipe(concat('big.min.css')).pipe(minifyCss()).pipe(gulp.dest(tvguide.app + 'css/'));
    gulp.src(tvguide.app + 'external/**/*.css').pipe(concat('external.min.css')).pipe(minifyCss()).pipe(gulp.dest(tvguide.app + 'css/'));
});


// сложим все html-шаблоны в один js-файл
gulp.task('ngtemplates', function () {
    return gulp.src(tvguide.app + 'ngtemplates/**/*.html').pipe(templateCache("templates.js", {module: "tvguideApp"})).pipe(gulp.dest(tvguide.app + 'js/'));
});

// обработаем все скрипты и склеим их в два файла - свой и чужой
gulp.task('scripts', ['ngtemplates'], function () {
    var uglifySettings = {
        compress: {
            screw_ie8: true,
            // join consecutive simple statements using the comma operator
            sequences: true,
            // remove unreachable code
            dead_code: true,
            // apply optimizations for if-s and conditional expressions
            conditionals: true,
            // various optimizations for boolean context, for example !!a ? b : c â†’ a ? b : c
            booleans: true,
            // drop unreferenced functions and variables
            unused: true,
            // optimizations for if/return and if/continue
            if_return: true,
            // join consecutive var statements
            join_vars: true,
            // discard calls to console.* functions
            drop_console: true
        }
    };

    gulp.src([tvguide.app + "external/**/*.js", "!" + tvguide.app + "external/jquery/**/*.js"])
        .pipe(order(["angular/**/*.js"], {base: tvguide.app + "external/"}))
        .pipe(concat('external.js'))
        .pipe(ngAnnotate()).pipe(uglify(uglifySettings))
        .pipe(gulp.dest(tvguide.app + 'js/final/'));

    gulp.src(tvguide.app + "external/jquery/**/*.js")
        .pipe(concat('jquery.js'))
        .pipe(ngAnnotate()).pipe(uglify(uglifySettings))
        .pipe(gulp.dest(tvguide.app + 'js/final/'));

    return gulp.src(tvguide.app + "js/*.js")
        .pipe(order([
            "routes.js",
            "application.js",
            "services.js",
            "controllers.js",
            "directives.js",
            "templates.js"
        ], {base: tvguide.app + "js/"})).pipe(concat('tvguide.js')).pipe(ngAnnotate()).pipe(uglify(uglifySettings)).pipe(gulp.dest(tvguide.app + 'js/final/'));
});

// обработка картинок
gulp.task('images', ['clean'], function () {
    return gulp.src(tvguide.app + 'images/**').pipe(imagemin({optimizationLevel: 5})).pipe(gulp.dest(tvguide.dist + 'images'));
});

// кладем шрифты и картинки рядом с css-ами
gulp.task('copy', ['clean', 'styles'], function () {
    return es.merge(
        gulp.src(tvguide.app + 'i18n/**').pipe(gulp.dest(tvguide.dist + 'i18n/')),

        gulp.src(tvguide.app + 'external/**/*.{woff,woff2,svg,ttf,eot}').pipe(flatten()).pipe(gulp.dest(tvguide.dist + 'css/fonts/')),

        gulp.src(tvguide.app + 'external/**/*.{png,gif}').pipe(flatten()).pipe(gulp.dest(tvguide.dist + 'css/images/'))
    );
});

//gulp.task('default', ['bower-installer', 'copy', 'less', 'styles', 'ngtemplates', 'scripts', 'images']);
gulp.task('default', ['copy', 'less', 'styles', 'ngtemplates', 'scripts', 'images']);