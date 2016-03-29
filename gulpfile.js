'use strict';

var gulp = require('gulp'),
//  prefix CSS with Autoprefixer
    prefix = require('gulp-autoprefixer'),
//  the gulp plugin <code>gulp-order</code> allows you to reorder a stream of files using the same syntax as of <code>gulp.src</code>
    order = require("gulp-order"),
//  construct pipes of streams of events (eventStream is like functional programming meets IO)
    es = require('event-stream'),
//  only pass through changed files
    changed = require('gulp-changed'),
//  live CSS Reload &amp; Browser Syncing
    browserSync = require('browser-sync'),
//  conditionally run a task
    gulpIf = require('gulp-if'),
//  expect files in pipes for gulp.js
    expect = require('gulp-expect-file'),
// prevent pipe breaking caused by errors from gulp plugins
    plumber = require('gulp-plumber'),

//  concatenates files
    concat = require('gulp-concat'),
//  static asset revisioning by appending content hash to filenames: unicorn.css =&gt; unicorn-d41d8cd98f.css
    rev = require('gulp-rev'),
//  remove or replace relative path for files
    flatten = require('gulp-flatten'),
//  a gulp plugin for removing files and folders
    clean = require('gulp-clean'),
//  a string replace plugin for gulp
    replace = require('gulp-replace'),
//  source map support for Gulp.js
    sourcemaps = require('gulp-sourcemaps'),

//  gulp plugin for sass
    sass = require('gulp-sass'),
//  minify css with clean-css
    cleancss = require('gulp-clean-css'),
//  gulp plugin to minify HTML
    htmlmin = require('gulp-htmlmin'),
//  replaces references to non-optimized scripts or stylesheets into a set of HTML files (or any templates/views)
    usemin = require('gulp-usemin'),
//  minify files with UglifyJS
    uglify = require('gulp-uglify'),
//  minify PNG, JPEG, GIF and SVG images
    imagemin = require('gulp-imagemin'),
//  a gulp plugin for using rigger ("include" other files into, //= footer.html)
    rigger = require('gulp-rigger'),

//  concatenates and registers AngularJS templates in the $templateCache
    templateCache = require('gulp-angular-templatecache'),
//  add angularjs dependency injection annotations with ng-annotate, instead [..., $scope, $timeout, function($scope, $timeout) -> [..., function($scope, $timeout)
    ngAnnotate = require('gulp-ng-annotate'),

//  JSHint plugin for gulp (JSHint is a tool that helps to detect errors and potential problems in your JavaScript code)
    jshint = require('gulp-jshint');


var config = {
    webapp: 'src/main/webapp/',
    content: this.webapp + 'content/',
    dist: 'target/webapp_dist/',
    distImagesDir: this.dist + "content/images/",
    test: 'src/test/javascript/',
    appDir: this.webapp + 'app/',
    sassDir: this.content + 'scss/',
    imagesDir: this.content + 'images/',
    cssDir: this.content + 'css',
    fontsDir: this.content + 'fonts',
    sassSrc: this.sassDir + '/**/*.{scss,sass}',
    htmlSrc: this.appDir + '**/*.html',
    imagesSrc: this.imagesDir + '/**/*',
    bower: this.webapp + 'bower_components/',
    targetTmp: 'target/tmp/',
    port: 9000,
    apiPort: 8080,
    liveReloadPort: 35729,
    uri: 'http://localhost:'
};

// clean task - очищаем папку dist
gulp.task('clean', function () {
    return gulp
        .src(config.dist, {read: false})
        .pipe(clean());
});

// task для сборки sass стилей
gulp.task('sass', function () {
    return es.merge(
        // берём всё sass файлы
        gulp.src(config.sassSrc)
            // мы ожидаем, что они есть
            .pipe(expect(config.sassSrc))
            //  будем заменять только те файлы, которые изменятся
            .pipe(changed(config.cssDir, {extension: '.css'}))
            // компилируем sass файлы
            .pipe(sass({includePaths: config.bower}).on('error', sass.logError))
            // сохраняем их в cssDir
            // todo: сохранять generated файлы в другую папку
            .pipe(gulp.dest(config.cssDir)),
        // скопируем font-ы из bower-а
        gulp.src(config.bower + '**/fonts/**/*.{woff,woff2,svg,ttf,eot,otf}')
            // будем заменять только те файлы, которые изменяются
            .pipe(changed(config.fontsDir))
            // исправляем относительные пути
            .pipe(flatten())
            // todo: сохранять generated файлы в другую папку
            // сохраняем шрифты в fontsDir
            .pipe(gulp.dest(config.fontsDir))
    );
});

// таск для подготовки стилей
gulp.task('styles', ['sass'], function () {
    // todo: тут какая то хрень, мы ж не таргет поменяли?
    return gulp.src(config.cssDir)
        // кидаем sync
        .pipe(browserSync.reload({stream: true}));
});

// таск для подготовки html файлов
gulp.task('html', function () {
    // берём все наши html шаблоны
    return gulp.src(config.htmlSrc)
        // минимизируем html файлы
        .pipe(htmlmin({collapseWhitespace: true}))
        // копируем их в файл templates.js
        .pipe(templateCache("templates.js", {module: "svoyakApp"}))
        // копируем в targetTmp
        // todo: сохранять в папку ко всем скриптам в таргете?
        .pipe(gulp.dest(config.targetTmp));
});

// todo: к коду выше использовать параметры из этого таска???
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

    // todo: написать таск по обработке скриптов
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

// todo: написать таск копирующий css-ы в таргет

// todo: подумать как быть с картинками и куда их складывать лучше и как это задать
// таск по обработке картинок
gulp.task('images', function () {
    // все из папки imagesSrc
    return gulp.src(config.imagesSrc)
        // только те файлы, которые изменились
        .pipe(changed(config.dist + 'content/images'))
        // минимизируем картинки
        .pipe(imagemin({optimizationLevel: 5, progressive: true, interlaced: true}))
        // проставляем ревизию
        .pipe(rev())
        // копируем в distImagesDir
        .pipe(gulp.dest(config.distImagesDir))
        // делаем mapsource
        .pipe(rev.manifest(config.revManifest, {
            base: config.dist,
            merge: true
        }))
        // сохраняем mapsource в dist
        .pipe(gulp.dest(config.dist))
        // кидаем sync
        .pipe(browserSync.reload({stream: true}));
});

// todo: разобраться с дефолтным таском 
//gulp.task('default', ['bower-installer', 'copy', 'less', 'styles', 'ngtemplates', 'scripts', 'images']);
gulp.task('default', ['copy', 'less', 'styles', 'ngtemplates', 'scripts', 'images']);

// todo: собственно написать copy скрипт?