# hacked this together to inspect important Vowpal Wabbit features in model

features = []

File.open("vw_features.txt", "r").each_line do |line|
    tokens = line.split(':')
    features << [tokens[0], tokens[2].to_f]
end

features.sort! do |a, b|
    diff = a[1].abs - b[1].abs
    if diff > 0
        -1
    elsif diff == 0
        0
    else
        1
    end
end

File.open("vw_features_sorted.txt", "w") do |file|
    features.each do |feat|
        file.write("#{feat[0]}:#{feat[1]}\n")
    end
end
