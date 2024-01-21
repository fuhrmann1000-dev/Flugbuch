<?php
// Report all PHP errors
error_reporting(E_ALL);

require_once('./include/Flug.php');
require_once('./include/FlugResult.php');
require_once('./include/FlugService.php');

$flugService = new FlugService();

// hier wird jetzt ausgewertet welche id im GET übergeben wurde und eventuell die Daten in die Formularfelder geladen
if (isset($_GET[Flug::ID])) {
	$flugResult = $flugService->selectFlugById($_GET[Flug::ID]);
} elseif (isset($_POST[Flug::ID])) {
	$flugResult = $flugService->selectFlugById($_POST[Flug::ID]);
}

if (isset($flugResult->flug)) {
	$flug = $flugResult->flug;
}

if (!isset($flug)) {
	$flug = new Flug();
}


// hier wird nun überprüft, ob das Formular abgesendet wurde und die Daten werden gespeichert, ist eine id vorhanden, dann update der Daten
// ist keine id da, dann wird ein neuer Datensatz angelegt

if (isset($_POST['save'])) {
	$flug = new Flug();
	$flug->id = $_POST[Flug::ID];
	$flug->datum = $_POST[Flug::DATUM];
	$flug->startzeit = $_POST[Flug::STARTZEIT];
	$flug->landezeit = $_POST[Flug::LANDEZEIT];
	$flug->muster = $_POST[Flug::MUSTER];
	$flug->kennzeichen = $_POST[Flug::KENNZEICHEN];
	$flug->pilot = $_POST[Flug::PILOT];
	$flug->besatzung = $_POST[Flug::BESATZUNG];
	$flug->gaeste = $_POST[Flug::GAESTE];
	$flug->flugart = $_POST[Flug::FLUGART];
	$flug->startplatz = $_POST[Flug::STARTPLATZ];
	$flug->zielplatz = $_POST[Flug::ZIELPLATZ];
	$flug->flugleiter = $_POST[Flug::FLUGLEITER];
	$flug->geschleppter = $_POST[Flug::GESCHLEPPTER];
	$flug->schlepphoehe = $_POST[Flug::SCHLEPPHOEHE];
	$flug->bemerkung = $_POST[Flug::BEMERKUNG];

	if (null == $flug->id) {
		$flugResult = $flugService->insertFlug($flug);
	} else {
		$flugResult = $flugService->updateFlug($flug);
	}

	$flug = $flugResult->flug; // hier könnte nach dem speichern eine id in den Flug eingetragen worden sein, das sichert die Idempotenz beim Speichern
} else {
	//echo 'not save';
}

?>
<!doctype html>
<html lang="de">

<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!--favicon-->
	<link rel="icon" href="assets/images/favicon-32x32.png" type="image/png" />
	<!--plugins-->
	<link href="assets/plugins/simplebar/css/simplebar.css" rel="stylesheet" />
	<link href="assets/plugins/perfect-scrollbar/css/perfect-scrollbar.css" rel="stylesheet" />
	<link href="assets/plugins/metismenu/css/metisMenu.min.css" rel="stylesheet" />
	<link rel="stylesheet" href="assets/plugins/notifications/css/lobibox.min.css" />
	<!-- loader-->
	<link href="assets/css/pace.min.css" rel="stylesheet" />
	<script src="assets/js/pace.min.js"></script>
	<!-- Bootstrap CSS -->
	<link href="assets/css/bootstrap.min.css" rel="stylesheet">
	<link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500&display=swap" rel="stylesheet">
	<link href="assets/css/app.css" rel="stylesheet">
	<link href="assets/css/icons.css" rel="stylesheet">

	<title>Flugbuch</title>
</head>

<body class="bg-theme bg-theme1">
	<!--wrapper-->
	<div class="wrapper">
		<!--sidebar wrapper -->
		<div class="sidebar-wrapper" data-simplebar="true">
			<div class="sidebar-header">
				<div>
					<img src="assets/images/logo-icon.png" class="logo-icon" alt="logo icon">
				</div>
				<div>
					<h4 class="logo-text">Hauptflugbuch</h4>
				</div>
				<div class="toggle-icon ms-auto"><i class='bx bx-arrow-to-left'></i>
				</div>
			</div>
			<!--navigation-->
			<?php include('inc/navigation.php') ?>
			<!--end navigation-->
		</div>
		<!--end sidebar wrapper -->
		<!--start header -->
		<?php include('inc/header.php') ?>
		<!--end header -->
		<!--start page wrapper -->
		<div class="page-wrapper">
			<div class="page-content">
				<!--breadcrumb-->
				<div class="page-breadcrumb d-none d-sm-flex align-items-center mb-3">
					<div class="breadcrumb-title pe-3">Flugbuch</div>
					<div class="ps-3">
						<nav aria-label="breadcrumb">
							<ol class="breadcrumb mb-0 p-0">
								<li class="breadcrumb-item"><a href="javascript:;"><i class="bx bx-home-alt"></i></a>
								</li>
								<li class="breadcrumb-item active" aria-current="page">Flugdaten bearbeiten</li>
							</ol>
						</nav>
					</div>
					<!--<div class="ms-auto">
						<div class="btn-group">
							<button type="button" class="btn btn-light">Settings</button>
							<button type="button" class="btn btn-light dropdown-toggle dropdown-toggle-split"
								data-bs-toggle="dropdown"> <span class="visually-hidden">Toggle Dropdown</span>
							</button>
							<div class="dropdown-menu dropdown-menu-right dropdown-menu-lg-end"> <a
									class="dropdown-item" href="javascript:;">Action</a>
								<a class="dropdown-item" href="javascript:;">Another action</a>
								<a class="dropdown-item" href="javascript:;">Something else here</a>
								<div class="dropdown-divider"></div> <a class="dropdown-item"
									href="javascript:;">Separated link</a>
							</div>
						</div>
					</div>-->
				</div>
				<!--end breadcrumb-->
				<div class="row">
					<div class="col-xl-12 mx-auto">
						<h6 class="mb-0 text-uppercase">Flugdaten bearbeiten</h6>
						<hr />
						<div class="card border-top border-0 border-4 border-white">
							<div class="card-body p-5">
								<div class="card-title d-flex align-items-center">
									<div><i class="bx bxs-user me-1 font-22 text-white"></i>
									</div>
									<h5 class="mb-0 text-white">Flugdaten bearbeiten</h5>
								</div>
								<hr>
								<form class="row g-3" method="post">
									<input type="hidden" name="<?= Flug::ID ?>" value="<?= $flug->id ?>" />
									<div class="col-md-4">
										<label for="<?= Flug::DATUM ?>" class="form-label">Datum</label>
										<input type="text" id="<?= Flug::DATUM ?>" name="<?= Flug::DATUM ?>"
											class="form-control" value="<?= $flug->datum ?>"
											title="Bitte gib das Datum im Format tt.mm.jjjj ein." />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::STARTZEIT ?>" class="form-label">Startzeit</label>
										<input type="text" id="<?= Flug::STARTZEIT ?>" name="<?= Flug::STARTZEIT ?>"
											class="form-control" value="<?= $flug->startzeit ?>"
											title="Bitte gib die Zeit im Format ss:mm ein." />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::LANDEZEIT ?>" class="form-label">Landezeit</label>
										<input type="text" id="<?= Flug::LANDEZEIT ?>" name="<?= Flug::LANDEZEIT ?>"
											class="form-control" value="<?= $flug->landezeit ?>"
											title="Bitte gib die Zeit im Format ss:mm ein." />
									</div>

									<div class="col-md-4">
										<label for="<?= Flug::MUSTER ?>" class="form-label">Muster</label>
										<input type="text" id="<?= Flug::MUSTER ?>" name="<?= Flug::MUSTER ?>"
											class="form-control" value="<?= $flug->muster ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::KENNZEICHEN ?>" class="form-label">Kennzeichen</label>
										<input type="text" id="<?= Flug::KENNZEICHEN ?>" name="<?= Flug::KENNZEICHEN ?>"
											class="form-control" value="<?= $flug->kennzeichen ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::PILOT ?>" class="form-label">Pilot</label>
										<input type="text" id="<?= Flug::PILOT ?>" name="<?= Flug::PILOT ?>"
											class="form-control" value="<?= $flug->pilot ?>" />
									</div>

									<div class="col-md-4">
										<label for="<?= Flug::BESATZUNG ?>" class="form-label">Besatzung</label>
										<input type="text" id="<?= Flug::BESATZUNG ?>" name="<?= Flug::BESATZUNG ?>"
											class="form-control" value="<?= $flug->besatzung ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::GAESTE ?>" class="form-label">Gäste</label>
										<input type="text" id="<?= Flug::GAESTE ?>" name="<?= Flug::GAESTE ?>"
											class="form-control" value="<?= $flug->gaeste ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::FLUGART ?>" class="form-label">Flugart</label>
										<input type="text" id="<?= Flug::FLUGART ?>" name="<?= Flug::FLUGART ?>"
											class="form-control" value="<?= $flug->flugart ?>" />
									</div>

									<div class="col-md-4">
										<label for="<?= Flug::STARTPLATZ ?>" class="form-label">Startplatz</label>
										<input type="text" id="<?= Flug::STARTPLATZ ?>" name="<?= Flug::STARTPLATZ ?>"
											class="form-control" value="<?= $flug->startplatz ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::ZIELPLATZ ?>" class="form-label">Zielplatz</label>
										<input type="text" id="<?= Flug::ZIELPLATZ ?>" name="<?= Flug::ZIELPLATZ ?>"
											class="form-control" value="<?= $flug->zielplatz ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::FLUGLEITER ?>" class="form-label">Flugleiter</label>
										<input type="text" id="<?= Flug::FLUGLEITER ?>" name="<?= Flug::FLUGLEITER ?>"
											class="form-control" value="<?= $flug->flugleiter ?>" />
									</div>

									<div class="col-md-4">
										<label for="<?= Flug::GESCHLEPPTER ?>" class="form-label">Geschleppter</label>
										<input type="text" id="<?= Flug::GESCHLEPPTER ?>"
											name="<?= Flug::GESCHLEPPTER ?>" class="form-control"
											value="<?= $flug->geschleppter ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::SCHLEPPHOEHE ?>" class="form-label">Schlepphöhe</label>
										<input type="text" id="<?= Flug::SCHLEPPHOEHE ?>"
											name="<?= Flug::SCHLEPPHOEHE ?>" class="form-control"
											value="<?= $flug->schlepphoehe ?>" />
									</div>
									<div class="col-md-4">
										<label for="<?= Flug::BEMERKUNG ?>" class="form-label">Bemerkung</label>
										<input type="text" id="<?= Flug::BEMERKUNG ?>" name="<?= Flug::BEMERKUNG ?>"
											class="form-control" value="<?= $flug->bemerkung ?>" />
									</div>

									<div class="col-12">
										<button type="submit" class="btn btn-light px-5" name="save">Speichern</button>
									</div>
								</form>
							</div>
						</div>

					</div>
				</div>
				<!--end row-->

			</div>
		</div>
		<!--end page wrapper -->
		<!--start overlay-->
		<div class="overlay toggle-icon"></div>
		<!--end overlay-->
		<!--Start Back To Top Button--> <a href="javaScript:;" class="back-to-top"><i
				class='bx bxs-up-arrow-alt'></i></a>
		<!--End Back To Top Button-->
		<footer class="page-footer">
			<p class="mb-0"></p>
		</footer>
	</div>
	<!--end wrapper-->
	<!--start switcher-->
	<div class="switcher-wrapper">
		<div class="switcher-btn"> <i class='bx bx-cog bx-spin'></i>
		</div>
		<div class="switcher-body">
			<div class="d-flex align-items-center">
				<h5 class="mb-0 text-uppercase">Theme Customizer</h5>
				<button type="button" class="btn-close ms-auto close-switcher" aria-label="Close"></button>
			</div>
			<hr />
			<p class="mb-0">Gaussian Texture</p>
			<hr>

			<ul class="switcher">
				<li id="theme1"></li>
				<li id="theme2"></li>
				<li id="theme3"></li>
				<li id="theme4"></li>
				<li id="theme5"></li>
				<li id="theme6"></li>
			</ul>
			<hr>
			<p class="mb-0">Gradient Background</p>
			<hr>

			<ul class="switcher">
				<li id="theme7"></li>
				<li id="theme8"></li>
				<li id="theme9"></li>
				<li id="theme10"></li>
				<li id="theme11"></li>
				<li id="theme12"></li>
				<li id="theme13"></li>
				<li id="theme14"></li>
				<li id="theme15"></li>
			</ul>
		</div>
	</div>
	<!--end switcher-->
	<!-- Bootstrap JS -->
	<script src="assets/js/bootstrap.bundle.min.js"></script>
	<!--plugins-->
	<script src="assets/js/jquery.min.js"></script>
	<script src="assets/plugins/simplebar/js/simplebar.min.js"></script>
	<script src="assets/plugins/metismenu/js/metisMenu.min.js"></script>
	<script src="assets/plugins/perfect-scrollbar/js/perfect-scrollbar.js"></script>
	<!--app JS-->
	<script src="assets/js/app.js"></script>

	<!--calendar-->
	<link rel="stylesheet" href="//code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css">
	<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.js"></script>
	<script src="assets/i18n/datepicker-de.js"></script>
	<script>
		$(function () {
			$("#<?= Flug::DATUM ?>").datepicker($.datepicker.regional["de"]);

		});
	</script>

	<script>
		$(function () {
			$("#<?= Flug::DATUM ?>").tooltip({
				show: {
					effect: "slideDown",
					delay: 250
				}
			});

		});
	</script>
	<!--notification js -->
	<script src="assets/plugins/notifications/js/lobibox.min.js"></script>
	<script src="assets/plugins/notifications/js/notifications.min.js"></script>
	<script>
		$(document).ready(function () {

			<?php
			if (FlugResult::INFO == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('success', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Information',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>
			<?php
			if (FlugResult::WARNING == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('warning', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Achtung',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>
			<?php
			if (FlugResult::ERROR == $flugResult->resultMessageType) {
				?>
				Lobibox.notify('error', {
					pauseDelayOnHover: true,
					continueDelayOnInactiveTab: false,
					position: 'top right',
					icon: 'bx bx-info-circle',
					title: 'Es ist ein Fehler aufgetreten',
					msg: '<?= $flugResult->resultMessage ?>'
				});
				<?php
			}
			?>

		});
	</script>
</body>

</html>