package Ape;

use 5.010001;
use strict;
use warnings;
use JSON;
use threads;


require Exporter;
use AutoLoader qw(AUTOLOAD);

our @ISA = qw(Exporter);

# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.

# This allows declaration	use Ape ':all';
# If you do not need this, moving things directly into @EXPORT or @EXPORT_OK
# will save memory.
our %EXPORT_TAGS = ( 'all' => [ qw(
	
) ] );

our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );

our @EXPORT = qw(
	
);

our $VERSION = '0.01';


# 1st argument is a boolean, true is remote execution, false is local
# 2nd argument is a list of hosts
# 3rd argument is the command flag to be passed to ape
# 4th command is the string of arguments to be passed with that command flag
sub ape
{
	my ($arrayref) = @_;
	my ($remote, $hosts, $command, $args) = @$arrayref;

	foreach my $arg (@_)
	{
		print "Argument: $arg \n";
	}

	if($remote)
	{
		$remote = "--remote";
	}
	else {
		$remote = "--local";
	}
	
	print "Executing: ape $remote $hosts $command $args \n";
        my @theOutput = `ape $remote $hosts $command $args`;
	print @theOutput;
}

sub getSlavesFromJSON
{
	my $filename = $_[0];
	local $/;
	open(FILE, $filename) or die "Can't read file 'filename' [$!]\n";  
	my $str = <FILE>; 
	close (FILE);
	my $json = new JSON;
	my $json_text = $json->allow_nonref->utf8->relaxed->escape_slash->loose->allow_singlequote->allow_barekey->decode($str);
	my $slaves = $json_text->{slaves};
	return @$slaves;
}

sub getRandomSlave
{
	my $filename = shift;
	my @slaves = getSlaves($filename);
	my $randomelement = $slaves[rand @slaves];
	return $randomelement;
}

sub getMastersFromJSON
{        
        my $filename = $_[0];
        local $/;
        open(FILE, $filename) or die "Can't read file 'filename' [$!]\n";
        my $str = <FILE>; 
        close (FILE);
        my $json = new JSON;
        my $json_text = $json->allow_nonref->utf8->relaxed->escape_slash->loose->allow_singlequote->allow_barekey->decode($str);   
        my $masters = $json_text->{masters};
        return @$masters;
}
    
sub getRandomMaster
{   
        my $filename = $_[0];
        my @masters = getMasters($filename);
        my $randomelement = $masters[rand @masters];
        return $randomelement;
}

sub getSlaves
{
	my $filename = $_[0];
	open(FILE, $filename) or die "Unable to open file [$!]\n";
	my @slaves = <FILE>;
	chomp(@slaves);
	close(FILE);
	return @slaves;
}

sub getMasters
{
        my $filename = $_[0];
        open(FILE, $filename) or die "Unable to open file [$!]\n";
        my @masters = <FILE>;
        close(FILE);
        return @masters;
}

sub runCommandsInParallel
{
	my @procs = ();
	# Fork off each new thread and start it
	foreach my $arg (@_)
        {
		push (@procs, threads->create(\&ape,$arg));
                print "Argument: @{$arg} \n";
        }
	foreach my $arg (@procs)
	{
		$arg->join;
	}
}

# Preloaded methods go here.

# Autoload methods go after =cut, and are processed by the autosplit program.

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

Ape - Perl extension for blah blah blah

=head1 SYNOPSIS

  use Ape;
  blah blah blah

=head1 DESCRIPTION

Stub documentation for Ape, created by h2xs. It looks like the
author of the extension was negligent enough to leave the stub
unedited.

Blah blah blah.

=head2 EXPORT

None by default.



=head1 SEE ALSO

Mention other useful documentation such as the documentation of
related modules or operating system documentation (such as man pages
in UNIX), or any relevant external documentation such as RFCs or
standards.

If you have a mailing list set up for your module, mention it here.

If you have a web site set up for your module, mention it here.

=head1 AUTHOR

=head1 COPYRIGHT AND LICENSE

 Copyright (c) 2012 Yahoo! Inc. All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. See accompanying LICENSE file.

=cut
