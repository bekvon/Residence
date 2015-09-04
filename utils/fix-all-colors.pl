#!/usr/bin/perl

use strict;

my %colors = (
	"0" => "BLACK",
	"1" => "DARK_BLUE",
	"2" => "DARK_GREEN",
	"3" => "DARK_AQUA",
	"4" => "DARK_RED",
	"5" => "DARK_PURPLE",
	"6" => "GOLD",
	"7" => "GRAY",
	"8" => "DARK_GRAY",
	"9" => "BLUE",
	"a" => "GREEN",
	"b" => "AQUA",
	"c" => "RED",
	"d" => "LIGHT_PURPLE",
	"e" => "YELLOW",
	"f" => "WHITE",
	"k" => "MAGIC",
	"l" => "BOLD",
	"m" => "STRIKETHROUGH",
	"n" => "UNDERLINE",
	"o" => "ITALIC",
	"r" => "RESET",
);
open (CMD, "find . -name \\*java|");
while( my $file = <CMD>) {
	chomp $file;
	my @lines = ();
	open (FILE, "<$file");
	while (my $line = <FILE>) {
		
		$line =~ s/\xc2//g;
		for my $c (keys %colors) {
			$line =~ s/\("\xa7$c"/\(ChatColor.$colors{$c}/g;
			$line =~ s/"\xa7$c"/ChatColor.$colors{$c}/g;
			$line =~ s/"\xa7$c(.)/ChatColor.$colors{$c}+"$1/g;
			$line =~ s/(.)\xa7$c"/$1"+ChatColor.$colors{$c}/g;
			$line =~ s/(.)\xa7$c(.)/$1"+ChatColor.$colors{$c}+"$2/g;
			$line =~ s/"\xa7"\+//g;
		}
		push @lines, $line;
		if ($line =~ /^package/) {
			push @lines, "import org.bukkit.ChatColor;\n";
		}
	}
	close FILE;
	open (FILE, ">$file");
	for (@lines) {
		print FILE $_;
		print $_ if (/\xa7/);
	}
	close FILE;
}
close CMD;
