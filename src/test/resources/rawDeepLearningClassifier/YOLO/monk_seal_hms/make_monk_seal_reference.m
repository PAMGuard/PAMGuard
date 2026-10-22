% Create a short test clip from the demo file and run the monk seal detector
% (the detection code from BiggerBlueHMSdetector.mlapp, Rasmussen, Sills,
% Parnell, Reichmuth 2026) on it to create the reference results for the
% PAMGuard unit test MonkSealYoloTest. Requires the Computer Vision Toolbox,
% the Image Processing Toolbox and the YOLO v4 support package.
%
% src - the demo file supplied with the MATLAB app.
% networkFile - BiggerBlueNetwork.mat supplied with the MATLAB app.
src = '20210513_003425_demo.wav';
networkFile = 'BiggerBlueNetwork.mat';
clipfile = fullfile(pwd, 'monk_seal_hms_clip_20210513_003425.wav');
[Sn, fs] = audioread(src, 'native');
startSample = 2*round(59.8*fs) + 1;           % start of the 3rd one minute segment
clipLen = round(129.8*fs);
Sclipn = Sn(startSample:startSample+clipLen-1);
audiowrite(clipfile, Sclipn, fs, 'BitsPerSample', 16);

S0 = load(networkFile);
detector = S0.detector;

Overlap = .2; L = 60; inputImageSize = [32*6 32*74 3];
fftwin = 4096; overlap=.7; mfr = 2048;

[S,fs] = audioread(clipfile);
N = length(S);
cstart = zeros (1000,1); cend = zeros(1000,1);
n=1; cstart(n) = 1; cend(n) = cstart(n)+round(L*fs);
while cstart(n)<N-round(L*fs)
    n = n+1;
    cstart(n) = cstart(n-1)+round((L-Overlap)*fs);
    cend(n) = cstart(n)+round(L*fs);
end
cstart(n+1:end) = []; cend(n+1:end) = [];
if cend(end)>N
    buf =zeros(round(L*fs),1);
    S = [S ; buf];
end
Sclip = S(cstart(1):cend(1));
[s, ~, ~] = spectrogram(Sclip,fftwin,round(overlap*fftwin),fftwin,fs,'yaxis');
sx = length(s(1,:));
idxR=length(Sclip)/sx;

det_segment = []; det_bbox = zeros(0,4); det_score = []; det_label = []; det_classprobs = zeros(0,6);
seg_stretchlim = zeros(numel(cstart),2);
app_tstart = []; app_tend = [];
for n=1:length(cstart)
    Sclip = S(cstart(n):cend(n));
    [s, fr, t] = spectrogram(Sclip,fftwin,round(overlap*fftwin),fftwin,fs,'yaxis');
    s = abs(s);
    maxfreq = find(fr<mfr,1,'last');
    minfreq = find (fr<16,1,'last');
    fr = fr(1:maxfreq);
    s = s(1:maxfreq,:);
    s(1:minfreq,:) = 0;
    seg_stretchlim(n,:) = stretchlim(flip(s,1))';
    ss1=imadjust(flip(s,1));
    slope2 = repmat(linspace(0,1,length(s(:,1)))',[1,length(s(1,:))]);
    slope3 = repmat(linspace(1,0,length(s(:,1)))',[1,length(s(1,:))]);
    clear ss
    ss(:,:,1) = ss1; ss(:,:,2) = ss1.*slope2; ss(:,:,3) = ss1.*slope3;
    I = imresize(ss,inputImageSize(1:2));
    [bboxes,scores,labels,info] = detect(detector,I);
    nd = numel(scores);
    det_segment = [det_segment; repmat(n-1, nd, 1)];
    det_bbox = [det_bbox; double(bboxes)];
    det_score = [det_score; double(scores)];
    det_label = [det_label; double(labels)];
    if nd>0, det_classprobs = [det_classprobs; double(info.ClassProbabilities)]; end
    % times as calculated by the MATLAB app (seconds from the start of the file)
    bnew = double(bboxes);
    app_tstart = [app_tstart; (cstart(n)+round(bnew(:,1).*idxR))./fs];
    app_tend = [app_tend; (cstart(n)+round(bnew(:,1).*idxR) + round(bnew(:,3).*idxR))./fs];
    fprintf('Segment %d: %d detections, stretchlim [%g %g]\n', n, nd, seg_stretchlim(n,1), seg_stretchlim(n,2));
end

seg_start = cstart - 1;   % zero based start sample of each segment
seg_len = round(L*fs) + 1;
class_names = detector.ClassNames;
save('monk_seal_hms_matlab_detections.mat', 'fs', 'seg_start', 'seg_len', 'seg_stretchlim', ...
    'det_segment', 'det_bbox', 'det_score', 'det_label', 'det_classprobs', 'class_names', ...
    'app_tstart', 'app_tend', '-v7');
disp(size(det_bbox));
