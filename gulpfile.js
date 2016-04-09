'use strict';

var gulp = require('gulp'),
//  construct pipes of streams of events (eventStream is like functional programming meets IO)
    es = require('event-stream'),
//  only pass through changed files
    changed = require('gulp-changed'),
//  conditionally run a task
    gulpIf = require('gulp-if'),

//  concatenates files
    concat = require('gulp-concat'),
//  static asset revisioning by appending content hash to filenames: unicorn.css =&gt; unicorn-d41d8cd98f.css
    rev = require('gulp-rev'),
// update links with rev number after gulp-rev
    revreplace = require("gulp-rev-replace"),
//  a gulp plugin for removing files and folders
    clean = require('gulp-clean'),

//  gulp plugin for sass
    sass = require('gulp-sass'),
//  minify css with clean-css
    csso = require('gulp-csso'),
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
// less
    less = require('gulp-less'),
//  prefix CSS with Autoprefixer
    prefix = require('gulp-autoprefixer'),

//  JSHint plugin for gulp (JSHint is a tool that helps to detect errors and potential problems in your JavaScript code)
    jshint = require('gulp-jshint'),
//  htmlhint wrapper for gulp to validate your HTML
    htmlhint = require('gulp-htmlhint'),
//  CSSLint plugin for gulp
    csslint = require('gulp-csslint');

/**
 * Dev mode or prod mode
 * @type {boolean}
 */
var isDev = true;


// target для html и images
var targetSource = 'build/webapp/';
// target для ресурсов (js, css, font)
var targetResources = 'build/resources/main/static/';
// source files
var webapp = 'src/main/webapp/';
// шаблоны для разных типов файлов
var sassPattern = '/**/*.{scss,sass}';
var lessPattern = '/**/*.less';
var cssPattern = '/**/*.css';
var jsPattern = '/**/*.js';
var imagesPattern = '/**/*';
var htmlPattern = '/**/*.html';
// path to bower components
var bowerDir = webapp + 'bower_components/';
// output style file
var styles = 'styles.css';
// output js file
var js = 'script.js';

/**
 * Переменная для "приложения" (отдельной части интерфейсов)
 * @param appName название
 * @returns {{css: string, sass: string, less: string, fonts: string, images: string, html: string, js: string, compiledCss: string, base: *}}
 */
function getApp(appName) {
    return {
        css: appName + 'css/',
        sass: appName + 'scss/',
        less: appName + 'less/',
        fonts: appName + 'fonts/',
        images: appName + 'images/',
        html: appName + 'html/',
        js: appName + 'js/',
        compiledCss: appName + 'compiledCss/',
        base: appName
    }
}

var apps = [getApp('app/game/'), getApp('app/admin/')];

// clean task - очищаем папку webapp в target
gulp.task('clean', function () {
    return gulp
    // можно не читать содержимое папки, нам всё равно щас её просто удалять
        .src([targetSource, targetResources], {read: false})
        // удаляем
        .pipe(clean());
});

/**
 * Запуск таска для всех "приложениЙ" (частей интерфейса)
 * @param taskFunction функция для каждого приложения
 * @returns {*|Map|Object} stream for return from gulp task
 */
var runTaskForAllApps = function (taskFunction) {
    // собираем массив из стримов
    var streams = [];
    for (var i = 0; i < apps.length; i++) {
        // для каждого таска кладем его стрим в массив
        streams.push(taskFunction(apps[i]));
    }
    // мержим стримы в один и возвращаем
    return es.merge(streams);
};

// task для сборки sass стилей
gulp.task('sass', function () {
    return runTaskForAllApps(function (app) {
        // берём всё sass файлы в папке app.sass
        return gulp.src(webapp + app.sass + sassPattern)
            //  будем заменять только те файлы, которые изменятся
            .pipe(changed(targetResources + app.compiledCss, {extension: '.css'}))
            // компилируем sass файлы
            .pipe(sass({includePaths: bowerDir}).on('error', sass.logError))
            // сохраняем их в css compiled dir
            .pipe(gulp.dest(targetResources + app.compiledCss));
    })
});

// task для сборки less-файлов
gulp.task('less', function () {
    return runTaskForAllApps(function (app) {
        // берём все less файлы в папке app.less
        return gulp.src(webapp + app.less + lessPattern)
            //  будем заменять только те файлы, которые изменятся
            .pipe(changed(targetResources + app.compiledCss, {extension: '.css'}))
            // компилируем less файлы
            .pipe(less())
            // сохраняем их в css compiled dir
            .pipe(gulp.dest(targetResources + app.compiledCss));
    })
});

// таск для копирования шрифтов
gulp.task('fonts', function () {
    return runTaskForAllApps(function (app) {
        // просто копируем шрифты из src to dest
        return gulp.src(webapp + app.fonts)
            .pipe(gulp.dest(targetResources + app.fonts));
    })
});

// таск для подготовки стилей
gulp.task('styles', ['sass', 'less'], function () {
    return runTaskForAllApps(function (app) {
        // берём все css из css complied dir и app css
        return gulp.src([webapp + app.css + cssPattern, targetResources + app.compiledCss + cssPattern])
            // склеиваем их через новую строку
            .pipe(concat(styles, {newLine: '\n'}))
            // пропускаем через автопрефиксер
            .pipe(prefix())
            // если !devMode, то минифицируем/оптимизируем
            .pipe(gulpIf(!isDev, csso()))
            // запускаем lint
            .pipe(csslint()).pipe(csslint.reporter())
            // если !devMode, то добавляем к файлу номер ревизии и не забываем про манифест
            .pipe(gulpIf(!isDev, rev()))
            .pipe(gulp.dest(targetResources + app.css))
            .pipe(gulpIf(!isDev, rev.manifest({merge: true}), gulp.dest(targetResources + app.base)))
    });
});

// таск для подготовки html файлов
gulp.task('html', function () {
    return runTaskForAllApps(function (app) {
            // берём все наши html шаблоны
            return gulp.src(webapp + app.html + htmlPattern)
                // пропускаем через rigger
                .pipe(rigger())
                // если !devMode, то минимизируем html файлы
                .pipe(gulpIf(!isDev, htmlmin({collapseWhitespace: true})))
                // запускаем hit
                .pipe(htmlhint()).pipe(htmlhint.reporter())
                // если !devMode, заменяем ссылки на ресурсы согласно манифесту
                .pipe(gulpIf(!isDev, revreplace({manifest: gulp.src(targetResources + app.base + "rev-manifest.json")})))
                .pipe(gulp.dest(targetSource + app.html));
        }
    )
});

// таск для подготовки скриптов
gulp.task('scripts', function () {
    return runTaskForAllApps(function (app) {
            // берём все скриптовые файлы
            return gulp.src(webapp + app.js + jsPattern)
                // склеиваем в один
                .pipe(concat(js, {newLine: '\n'}))
                // hinter
                .pipe(jshint()).pipe(jshint.reporter())
                // если !devMode, то минифицируем
                .pipe(gulpIf(!isDev, uglify()))
                // если !devMode, то добавляем к файлу номер ревизии и не забываем про манифест
                .pipe(gulpIf(!isDev, rev()))
                .pipe(gulp.dest(targetResources + app.js))
                .pipe(gulpIf(!isDev, rev.manifest({merge: true}), gulp.dest(targetResources + app.base)));
        }
    )
});

// таск по обработке картинок
gulp.task('images', function () {
    return runTaskForAllApps(function (app) {
            // все из папки images
            return gulp.src(webapp + app.images + imagesPattern)
                // только те файлы, которые изменились
                .pipe(changed(targetSource + app.images))
                // минимизируем картинки
                .pipe(imagemin({optimizationLevel: 5, progressive: true, interlaced: true}))
                // если !devMode, то добавляем к файлу номер ревизии и не забываем про манифест
                .pipe(gulpIf(!isDev, rev()))
                .pipe(gulp.dest(targetSource + app.images))
                .pipe(gulpIf(!isDev, rev.manifest({merge: true}), gulp.dest(targetResources + app.base)));
        }
    )
});

// таск отслеживающий изменения во всех файлах (ну кроме шрифтов)
gulp.task('watch', function () {
    for (var i = 0; i < apps.length; i++) {
        var app = apps[i];
        gulp.watch(webapp + app.css + cssPattern, ['styles']);
        gulp.watch(webapp + app.less + lessPattern, ['styles']);
        gulp.watch(webapp + app.sass + sassPattern, ['styles']);
        gulp.watch(webapp + app.css + cssPattern, ['sass']);
        gulp.watch(webapp + app.images + imagesPattern, ['images']);
        gulp.watch(webapp + app.js + jsPattern, ['scripts']);
        gulp.watch(webapp + app.html + htmlPattern, ['html']);
    }
});

// основной таск для сборки (по умолчанию dev режим)
gulp.task('build', ['less', 'sass', 'styles', 'fonts', 'scripts', 'images', 'html']);

// таск для сборки в dev режиме
gulp.task('dev', ['build']);

// таск для сборки в prod режиме
gulp.task('prod', function () {
    // скидываем флаг
    isDev = false;
    // вызываем основной таск
    gulp.start('build');
});
