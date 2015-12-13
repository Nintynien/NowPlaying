package com.skylerbock.nowplaying.movie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sbock on 12/5/15.
 */
public class Movie implements Parcelable {
    private String title = "";
    private String posterUrl = "";
    private String trailerUrl = "";
    private String imdbId;
    private String year;
    private String plot;
    private String director;
    private String cast;
    private String genre;
    private String duration;
    private String mpaaRating;
    private String imdbRating;

    public Movie(String title, String posterUrl, String trailerUrl, String imdbId, String year, String plot, String director, String cast, String genre, String duration, String mpaaRating, String imdbRating) {

        this.title = title;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.imdbId = imdbId;
        this.year = year;
        this.plot = plot;
        this.director = director;
        this.cast = cast;
        this.genre = genre;
        this.duration = duration;
        this.mpaaRating = mpaaRating;
        this.imdbRating = imdbRating;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getPlot() {
        return plot;
    }

    public String getYear() { return year; }

    public String getDirector() {
        return director;
    }

    public String getCast() {
        return cast;
    }

    public String getGenre() {
        return genre;
    }

    public String getDuration() {
        return duration;
    }

    public String getMpaaRating() {
        return mpaaRating;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    protected Movie(Parcel in) {
        title = in.readString();
        posterUrl = in.readString();
        trailerUrl = in.readString();
        imdbId = in.readString();
        year = in.readString();
        plot = in.readString();
        director = in.readString();
        cast = in.readString();
        genre = in.readString();
        duration = in.readString();
        mpaaRating = in.readString();
        imdbRating = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(posterUrl);
        dest.writeString(trailerUrl);
        dest.writeString(imdbId);
        dest.writeString(year);
        dest.writeString(plot);
        dest.writeString(director);
        dest.writeString(cast);
        dest.writeString(genre);
        dest.writeString(duration);
        dest.writeString(mpaaRating);
        dest.writeString(imdbRating);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
